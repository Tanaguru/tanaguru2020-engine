package com.tanaguru.runner;


import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class AuditRunnerPage extends AbstractAuditRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRunnerPage.class);
    private Collection<String> urls;

    public AuditRunnerPage(
            Collection<TanaguruTest> tanaguruTests,
            Audit audit,
            Collection<String> urls,
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
        this.urls = urls;
    }

    @Override
    protected void runImpl() {
        for (String url : urls) {
            if (super.isStop()) {
                LOGGER.warn("[Audit {}] Interrupting current audit", super.getAudit().getId());
                break;
            } else {
                webDriverGet(url);
            }
        }
    }
}
