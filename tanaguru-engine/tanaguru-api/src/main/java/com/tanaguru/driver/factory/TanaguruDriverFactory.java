package com.tanaguru.driver.factory;

import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;
import java.util.Optional;

/**
 * @author rcharre
 */
public interface TanaguruDriverFactory {
    /**
     * Create a TanaguruDriver from the audit parameters map
     * @return A configured TanaguruDriver
     */
    Optional<RemoteWebDriver> create();
}
