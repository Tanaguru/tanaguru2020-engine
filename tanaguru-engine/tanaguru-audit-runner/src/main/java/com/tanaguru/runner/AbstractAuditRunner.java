package com.tanaguru.runner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.helper.ImageHelper;
import com.tanaguru.runner.listener.AuditRunnerListener;
import com.tanaguru.webextresult.WebextPageResult;
import org.openqa.selenium.Dimension;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.*;
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
import java.util.HashMap;

public abstract class AbstractAuditRunner implements AuditRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAuditRunner.class);

    private static final int MAX_CANVAS_SIZE = 32767;
    private static final int DEFAULT_WINDOW_SIZE = 1080;
    private static final float SCREENSHOT_QUALITY_COMPRESSION = 1f;

    private final Audit audit;
    private final RemoteWebDriver tanaguruDriver;
    private final Collection<AuditRunnerListener> listeners = new ArrayList<>();
    private final Collection<String> visitedUrl = new ArrayList<>();
    private final Collection<Integer> resolutions;
    private final String basicAuthUrl;
    private final String basicAuthLogin;
    private final String basicAuthPassword;
    private final boolean enableScreenShot;
    private final Gson gson = new Gson();
    private final long waitTime;
    private final HashMap<String,HashMap<String, StringBuilder>> coreScript;

    private boolean stop = false;
    private int currentRank = 1;

    public AbstractAuditRunner(
            Audit audit,
            RemoteWebDriver driver,
            HashMap<String,HashMap<String, StringBuilder>> coreScript,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot) {
        this.audit = audit;
        this.tanaguruDriver = driver;
        this.waitTime = waitTime;
        this.resolutions = resolutions;
        this.basicAuthUrl = basicAuthUrl;
        this.basicAuthLogin = basicAuthLogin;
        this.basicAuthPassword = basicAuthPassword;
        this.enableScreenShot = enableScreenShot;
        this.coreScript = coreScript;
    }

    public WebDriver getTanaguruDriver() {
        return tanaguruDriver;
    }

    public final void run() {
        LOGGER.info("[Audit {}] Start runner", audit.getId());
        for (AuditRunnerListener tanaguruDriverListener : listeners) {
            tanaguruDriverListener.onAuditStart(this);
        }

        try {
            //Use basic auth
            if (!basicAuthUrl.isEmpty() && !basicAuthLogin.isEmpty() && !basicAuthPassword.isEmpty()) {
                URL url = new URL(basicAuthUrl);
                StringBuilder strb = new StringBuilder();
                strb.append(url.getProtocol());
                strb.append("://");
                strb.append(basicAuthLogin);
                strb.append(":");
                strb.append(basicAuthPassword);
                strb.append("@");
                strb.append(url.getHost());
                if (url.getPort() != -1) {
                    strb.append(":");
                    strb.append(url.getPort());
                }
                strb.append(url.getPath());
                tanaguruDriver.get(strb.toString());
            }
            this.runImpl();
        } catch (Exception e) {
            e.printStackTrace();
            auditLog(EAuditLogLevel.ERROR, "Tanaguru encountered an issue, please contact an administrator of the platform.");
        }

        try {
            LOGGER.debug("[Audit {}] Closing webdriver", audit.getId());
            tanaguruDriver.quit();
        } catch (Exception e) {
            LOGGER.error("[Audit {}] Error while closing webdriver : {}", audit.getId(), e.getMessage());
            auditLog(EAuditLogLevel.ERROR, "Error while closing webdriver : " + e.getMessage());
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
        if (firstHash != -1 && lastSlash != -1 && lastSlash < firstHash) {
            url = url.substring(0, firstHash);
        }
        //warning http:// and http://www. same page
        boolean alreadyVisited = false;
        if(visitedUrl.contains(url) || visitedUrl.contains(url.replace("www.", "")) || visitedUrl.contains(url.replace("://", "://www."))){
            alreadyVisited = true;
        }
        

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
        for (Integer width : resolutions) {
            Dimension resolution = new Dimension(width, DEFAULT_WINDOW_SIZE);
            String definiteName = name + "_" + width;

            tanaguruDriver.manage().window().setSize(resolution);
            String screenshot = null;
            if (enableScreenShot) {
                LOGGER.debug("[Audit {}] Take screenshot for url {}", audit.getId(), url);

                try {
                    screenshot = takeScreenshot(resolution);
                } catch (IOException e) {
                    LOGGER.error("[Audit {}] Failed to take screenshot on page {} cause : {}", audit.getId(), url, e.getMessage());
                    auditLog(EAuditLogLevel.ERROR, "Failed to take screenshot on page " + url + " cause : " + e.getMessage());
                }
            }

            try {
                String result = "";
                ArrayList<JSONObject> results = new ArrayList<>();
                
                for(String refKey : coreScript.keySet()) {
                    for(String category : coreScript.get(refKey).keySet()) {
                        auditLog(EAuditLogLevel.INFO, "Running tests - "+category+" - "+refKey);
                        StringBuilder script = coreScript.get(refKey).get(category);
                        result = (String) tanaguruDriver.executeScript(script.toString());
                        results.add(new JSONObject(result));
                    }
                }
                
                JSONArray tests = new JSONArray();
                JSONArray tags = new JSONArray();
                for(JSONObject res : results) {
                    for(Object test : res.getJSONArray("tests")) {
                        tests.put(test);
                    }
                    for(Object tag : res.getJSONArray("tags")) {
                        tags.put(tag);
                    }
                }
                JSONObject finalRes = new JSONObject();
                finalRes.put("tests", tests);
                finalRes.put("tags", tags);
                String source = tanaguruDriver.getPageSource();
                for (AuditRunnerListener tanaguruDriverListener : listeners) {
                    tanaguruDriverListener.onAuditNewPage(this, definiteName, url, currentRank, gson.fromJson(finalRes.toString(), WebextPageResult.class), screenshot, source);
                }
                currentRank++;
            } catch (WebDriverException e) {
                LOGGER.error("[Audit {}] Script error on page {}\n{}\n", audit.getId(), url, e.getMessage());
                auditLog(EAuditLogLevel.ERROR, "Error during script execution on page " + url + "\n"
                        + e.getMessage());
            } catch (JsonSyntaxException e) {
                LOGGER.error("[Audit {}] Error while parsing result on page {}\n{}\n", audit.getId(), url, e.getMessage());
                auditLog(EAuditLogLevel.ERROR, "Error while parsing result on page " + url + "\n"
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
        /*int yOffset = 0;
        while(yOffset < resolution.height){
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

    public void addListener(AuditRunnerListener auditRunnerListener) {
        LOGGER.debug("[Audit {}] Listener added to runner", audit.getId());
        this.listeners.add(auditRunnerListener);
    }

    public void removeListener(AuditRunnerListener auditRunnerListener) {
        LOGGER.debug("[Audit {}] Listener removed from runner", audit.getId());
        this.listeners.remove(auditRunnerListener);
    }

    public boolean isStop() {
        return stop;
    }

    public void interrupt() {
        stop = true;
    }

    public void webDriverGet(String url) {
        try {
            tanaguruDriver.get(url);
        } catch (TimeoutException e) {
            LOGGER.debug("Webdriver timeout for url {}", url);
            auditLog(EAuditLogLevel.WARNING, "Webdriver automatic wait time timed out when loading page " + url + ". This is not an error, it just means that the browser does not have the time to load the page with your current configuration. If you see this WARNING more often you should probably increase the wait time in your server configuration and check how long your website takes to load on a fresh installed browser.");
        }

        try {
            LOGGER.debug("Custom wait time {}", waitTime);
            auditLog(EAuditLogLevel.INFO, "Custom wait time " + waitTime);
            Thread.sleep(waitTime);
            onGetNewPage(url, tanaguruDriver.getTitle(), false);
        } catch (InterruptedException e) {
            LOGGER.debug("Waiting time interrupted for url {}", url);
            auditLog(EAuditLogLevel.ERROR, "Thread interrupted while waiting content to load");
        }
    }

    protected void auditLog(EAuditLogLevel logLevel, String message) {
        for (AuditRunnerListener tanaguruDriverListener : listeners) {
            tanaguruDriverListener.onAuditLog(this, logLevel, message);
        }
    }

    public RemoteWebDriver getDriver() {
        return tanaguruDriver;
    }


    public Audit getAudit() {
        return audit;
    }
}
