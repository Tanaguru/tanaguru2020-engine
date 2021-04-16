package com.tanaguru.runner;

import com.google.gson.Gson;
import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.helper.ImageHelper;
import com.tanaguru.runner.listener.AuditRunnerListener;
import com.tanaguru.webextresult.WebextPageResult;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;

public abstract class AbstractAuditRunner implements AuditRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAuditRunner.class);

    private static final int MAX_CANVAS_SIZE = 32767;
    private static final int DEFAULT_WINDOW_SIZE = 1080;
    private static final float SCREENSHOT_QUALITY_COMPRESSION = 1f;

    private Audit audit;
    private RemoteWebDriver tanaguruDriver;
    private String coreScript;

    private int currentRank = 1;

    private Collection<AuditRunnerListener> listeners = new ArrayList<>();
    private Collection<String> visitedUrl = new ArrayList<>();
    private Collection<TanaguruTest> tanaguruTests;
    private Collection<Integer> resolutions;

    private String testScript;

    private String basicAuthUrl;
    private String basicAuthLogin;
    private String basicAuthPassword;
    private boolean enableScreenShot;

    private Gson gson = new Gson();

    private boolean stop = false;

    private long waitTime;

    public AbstractAuditRunner(
            Collection<TanaguruTest> tanaguruTests,
            Audit audit,
            RemoteWebDriver driver,
            String coreScript,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot) {
        this.audit = audit;
        this.tanaguruDriver = driver;
        this.coreScript = coreScript;
        this.tanaguruTests = tanaguruTests;
        this.waitTime = waitTime;
        this.resolutions = resolutions;
        this.basicAuthUrl = basicAuthUrl;
        this.basicAuthLogin = basicAuthLogin;
        this.basicAuthPassword = basicAuthPassword;
        this.enableScreenShot = enableScreenShot;
    }

    public WebDriver getTanaguruDriver() {
        return tanaguruDriver;
    }

    public final void run() {
        this.testScript = createFullScripts();

        LOGGER.info("[Audit {}] Start runner", audit.getId());
        for (AuditRunnerListener tanaguruDriverListener : listeners) {
            tanaguruDriverListener.onAuditStart(this);
        }

        try{
            //Use basic auth
            if(!basicAuthUrl.isEmpty() && !basicAuthLogin.isEmpty() && !basicAuthPassword.isEmpty()){
                URL url = new URL(basicAuthUrl);
                StringBuilder strb = new StringBuilder();
                strb.append(url.getProtocol());
                strb.append("://");
                strb.append(basicAuthLogin);
                strb.append(":");
                strb.append(basicAuthPassword);
                strb.append("@");
                strb.append(url.getHost());
                if(url.getPort() != -1){
                    strb.append(":");
                    strb.append(url.getPort());
                }
                strb.append(url.getPath());
                tanaguruDriver.get(strb.toString());
            }
            this.runImpl();
        }catch (Exception e){
            LOGGER.error("Error during run : " + e.getMessage());
            auditLog(EAuditLogLevel.ERROR, "Error during run : " + e.getMessage());
        }

        try{
            LOGGER.debug("[Audit {}] Closing webdriver", audit.getId());
            tanaguruDriver.quit();
        }catch (Exception e){
            LOGGER.error("[Audit {}] Error while closing webdriver", audit.getId());
            auditLog(EAuditLogLevel.ERROR, "Error while closing webdriver");
        }

        LOGGER.info("[Audit {}] Runner ended", audit.getId());
        for (AuditRunnerListener auditRunnerListener : listeners) {
            auditRunnerListener.onAuditEnd(this);
        }
    }

    protected abstract void runImpl();

    public final void onGetNewPage(String url, String name, boolean auditIfAlreadyVisited) {
        int firstHash = url.indexOf('#');
        int lastSlash = url.lastIndexOf('/');

        //Cut anchor but keep url for framework that use hash in url as Vue.js
        if(firstHash != -1 && lastSlash != -1 && lastSlash < firstHash){
            url = url.substring(0, firstHash);
        }

        boolean alreadyVisited = visitedUrl.contains(url);

        if (alreadyVisited) {
            if (auditIfAlreadyVisited) {
                auditPage(url, name);
            }
        } else {
            visitedUrl.add(url);
            auditPage(url, name);
        }
    }

    private void auditPage(String url, String name) {
        LOGGER.info("[Audit {}] Execute tests on page {}", audit.getId(), url);
        for(Integer width : resolutions) {
            Dimension resolution = new Dimension(width, DEFAULT_WINDOW_SIZE);
            String definiteName = name + "_" + width;

            tanaguruDriver.manage().window().setSize(resolution);
            String screenshot = null;
            if(enableScreenShot){
                LOGGER.debug("[Audit {}] Take screenshot for url {}", audit.getId(), url);

                try {
                    screenshot = takeScreenshot(resolution);
                } catch (IOException e) {
                    LOGGER.error("[Audit {}] Failed to take screenshot on page {} cause : {}", audit.getId(), url, e.getMessage());
                    auditLog(EAuditLogLevel.ERROR, "Failed to take screenshot on page " + url + " cause : " + e.getMessage());
                }
            }

            try {
                String result = (String) tanaguruDriver.executeScript(testScript);
                String source = tanaguruDriver.getPageSource();
                for (AuditRunnerListener tanaguruDriverListener : listeners) {
                    tanaguruDriverListener.onAuditNewPage(this, definiteName, url, currentRank, gson.fromJson(result, WebextPageResult.class), screenshot, source);
                }
                currentRank++;
            } catch (Exception e) {
                LOGGER.error("[Audit {}] Script error on page {}\n{}\n", audit.getId(), url, e.getMessage());
                auditLog(EAuditLogLevel.ERROR, "Error during script execution on page " + url + "\n"
                        + e.getMessage());
            }
        }
    }

    private String takeScreenshot(Dimension resolution) throws IOException {
        BufferedImage screenshotImage = null;
        BufferedImage screenImage = ImageHelper.getFromByteArray(tanaguruDriver.getScreenshotAs(OutputType.BYTES));
        BufferedImage jpgImage = new BufferedImage(screenImage.getWidth(), screenImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        jpgImage.createGraphics().drawImage(screenImage, 0, 0, Color.BLACK, null);
        jpgImage = ImageHelper.scaleImage(jpgImage, 0.5f);
        screenshotImage = ImageHelper.appendImages(screenshotImage, jpgImage);
        int yOffset = 0;
        /*while(yOffset < resolution.height){
            if(resolution.height - yOffset < DEFAULT_WINDOW_SIZE * 2){
                tanaguruDriver.manage().window().setSize(new Dimension(resolution.width, resolution.height - yOffset));
                tanaguruDriver.executeScript("window.scrollTo(0, " + yOffset + ");");
            }
            BufferedImage screenImage = ImageHelper.getFromByteArray(tanaguruDriver.getScreenshotAs(OutputType.BYTES));
            BufferedImage jpgImage = new BufferedImage(screenImage.getWidth(), screenImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            jpgImage.createGraphics().drawImage(screenImage, 0, 0, Color.BLACK, null);
            jpgImage = ImageHelper.scaleImage(jpgImage, 0.2f);

            screenshotImage = ImageHelper.appendImages(screenshotImage, jpgImage);

            yOffset += screenImage.getHeight();
            tanaguruDriver.executeScript("window.scrollTo(0, " + yOffset + ");");
        }
        tanaguruDriver.executeScript("window.scrollTo(0, 0);");*/
        return Base64.getEncoder().encodeToString(
                ImageHelper.compressImage(screenshotImage, SCREENSHOT_QUALITY_COMPRESSION, "jpg"));
    }

    private String createFullScripts() {
        StringBuilder strb = new StringBuilder();
        Gson gson = new Gson();
        LOGGER.debug("[Audit {}] Generating script", audit.getId());
        strb.append(coreScript);
        strb.append("\n");
        for (TanaguruTest tanaguruTest : this.tanaguruTests) {
            strb.append("\ncreateTanaguruTest({id:").append(tanaguruTest.getId());
            strb.append(",\nname:`").append(tanaguruTest.getName()).append("`");
            strb.append(",\nquery:`").append(tanaguruTest.getQuery()).append("`");
            strb.append(",\ntags:").append(gson.toJson(tanaguruTest.getTags()));


            if(tanaguruTest.getExpectedNbElements() != null){
                strb.append(",\nexpectedNbElements:");
                try {
                    strb.append(
                            Integer.parseInt(tanaguruTest.getExpectedNbElements()));
                }catch (NumberFormatException nfe){
                    strb.append(gson.toJson(tanaguruTest.getExpectedNbElements()));
                }
            }

            if(tanaguruTest.getDescription() != null){
                strb.append(",\ndescription:\"").append(tanaguruTest.getDescription()).append("\"");
            }

            if(tanaguruTest.getFilter() != null){
                strb.append(",\nfilter:").append("new Function('item', 'HTML', `return ").append(tanaguruTest.getFilter()).append("(item, HTML)`)");
            }

            if(tanaguruTest.getAnalyzeElements() != null){
                strb.append(",\nanalyzeElements:").append("new Function('collection', 'HTML', `return ").append(tanaguruTest.getAnalyzeElements()).append("(collection, HTML)`)");
            }
            strb.append("});");
        }
        strb.append("\nreturn JSON.stringify(loadTanaguruTests());");
        auditLog(EAuditLogLevel.INFO, "Tests script for audit created");
        return strb.toString();
    }

    public void addListener(AuditRunnerListener auditRunnerListener) {
        LOGGER.debug("[Audit {}] Listener added to runner", audit.getId());
        this.listeners.add(auditRunnerListener);
    }

    public void removeListener(AuditRunnerListener auditRunnerListener) {
        LOGGER.debug("[Audit {}] Listener removed from runner", audit.getId());
        this.listeners.remove(auditRunnerListener);
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void webDriverGet(String url) {
        try {
            tanaguruDriver.get(url);
        } catch (TimeoutException e) {
            LOGGER.debug("Webdriver timeout for url {}", url);
            auditLog(EAuditLogLevel.WARNING, "Webdriver automatic wait time timed out when loading page " + url + ". This is not an error, it just means that the browser does not have the time to load the page with your current configuration. If you see this WARNING more often you should probably increase the wait time in your server configuration and check how long your website takes to load on a fresh installed browser.");
        }

        try {
            LOGGER.debug("Custom wait time ", waitTime);
            auditLog(EAuditLogLevel.INFO, "Custom wait time " + waitTime);
            Thread.sleep(waitTime);
            onGetNewPage(url, tanaguruDriver.getTitle(), false);
        } catch (InterruptedException e) {
            LOGGER.debug("Waiting time interrupted for url {}", url);
            auditLog(EAuditLogLevel.ERROR, "Thread interrupted while waiting content to load");
        }
    }

    protected void auditLog(EAuditLogLevel logLevel, String message){
        for (AuditRunnerListener tanaguruDriverListener : listeners) {
            tanaguruDriverListener.onAuditLog(this, logLevel, message);
        }
    }

    public RemoteWebDriver getDriver(){
        return tanaguruDriver;
    }
    
    
}
