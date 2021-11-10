package com.tanaguru.runner;


import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;

public class AuditRunnerPage extends AbstractAuditRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRunnerPage.class);
    private final Collection<String> urls;

    public AuditRunnerPage(
            Audit audit,
            Collection<String> urls,
            RemoteWebDriver driver,
            HashMap<String,String> coreScript,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot) {
        super(audit, driver, coreScript, waitTime, resolutions, basicAuthUrl, basicAuthLogin, basicAuthPassword, enableScreenShot);
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
