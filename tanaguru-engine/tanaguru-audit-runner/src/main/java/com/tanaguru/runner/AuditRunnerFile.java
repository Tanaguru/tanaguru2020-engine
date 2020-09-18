package com.tanaguru.runner;


import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

public class AuditRunnerFile extends AbstractAuditRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRunnerFile.class);
    private String fileContent;

    public AuditRunnerFile(
            Collection<TanaguruTest> tanaguruTests,
            Audit audit,
            String fileContent,
            RemoteWebDriver driver,
            String coreScript,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot) {
        super(
                tanaguruTests,
                audit,
                driver,
                coreScript,
                waitTime,
                resolutions,
                basicAuthUrl,
                basicAuthLogin,
                basicAuthPassword,
                enableScreenShot);
        this.fileContent = fileContent;
    }

    @Override
    protected void runImpl() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("webresource-" + getAudit().getId(), "html");
        } catch (IOException e) {
            LOGGER.error("[Audit {}] Error while creating temporary file for the runner", getAudit().getId());
        }

        if (tempFile != null && tempFile.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
                bw.write(fileContent);
            } catch (IOException e) {
                LOGGER.error("[Audit {}] Error while initializing temporary file for the runner", getAudit().getId());
            }

            webDriverGet("file://" + tempFile.getAbsolutePath());

            try {
                Files.delete(tempFile.toPath());
            } catch (IOException e) {
                LOGGER.error("[Audit {}] Error while deleting temporary file for the runner", getAudit().getId());
            }
        }
    }
}
