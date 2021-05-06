package com.tanaguru.runner;

import com.tanaguru.crawler.TanaguruCrawlerController;
import com.tanaguru.crawler.listener.TanaguruCrawlerListener;
import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class AuditRunnerSite extends AbstractAuditRunner implements TanaguruCrawlerListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRunnerSite.class);
    private TanaguruCrawlerController crawlerController;

    public AuditRunnerSite(
            Collection<TanaguruTest> tanaguruTests,
            Audit audit,
            TanaguruCrawlerController crawlerController,
            RemoteWebDriver driver,
            String coreScript,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot) {
        super(tanaguruTests, audit, driver, coreScript, waitTime, resolutions, basicAuthUrl, basicAuthLogin, basicAuthPassword, enableScreenShot);
        this.crawlerController = crawlerController;
    }

    @Override
    protected void runImpl() {
        crawlerController.addListener(this);
        crawlerController.run();
        crawlerController.waitUntilFinish();
    }

    @Override
    public synchronized void onCrawlNewPage(String url) {
        try{
            webDriverGet(url);
        }catch (Exception e){
            LOGGER.error("Error happened while auditing page {} : {}", url, e.getMessage());
            auditLog(EAuditLogLevel.ERROR, "Error happened while auditing page " + url + " : " + e.getMessage());
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (super.isStop()) {
            crawlerController.shutdown();
        }
    }
}
