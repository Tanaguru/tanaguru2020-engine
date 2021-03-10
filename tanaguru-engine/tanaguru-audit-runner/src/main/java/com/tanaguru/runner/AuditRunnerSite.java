package com.tanaguru.runner;

import com.tanaguru.crawler.TanaguruCrawlerController;
import com.tanaguru.crawler.listener.TanaguruCrawlerListener;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Collection;

public class AuditRunnerSite extends AbstractAuditRunner implements TanaguruCrawlerListener {
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
            boolean enableScreenShot,
            String cssQuery) {
        super(tanaguruTests, audit, driver, coreScript, waitTime, resolutions, basicAuthUrl, basicAuthLogin, basicAuthPassword, enableScreenShot, cssQuery);
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
    public void setStop(boolean stop) {
        if (stop) {
            crawlerController.shutdown();
        }
        super.setStop(stop);
    }
}
