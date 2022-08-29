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
    private final TanaguruCrawlerController crawlerController;

    public AuditRunnerSite(
            Audit audit,
            TanaguruCrawlerController crawlerController,
            RemoteWebDriver driver,
            String coreScript,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot,
            String accessibilityScript) {
        super(audit, driver, coreScript, waitTime, resolutions, basicAuthUrl, basicAuthLogin, basicAuthPassword, enableScreenShot, accessibilityScript);
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
            webDriverGet(url);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (super.isStop()) {
            crawlerController.shutdown();
        }
    }
}
