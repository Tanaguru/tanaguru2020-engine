package com.tanaguru.driver.factory;

import com.tanaguru.domain.constant.BrowserName;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Optional;

/**
 * @author rcharre
 */
public interface TanaguruDriverFactory {
    /**
     * Create a TanaguruDriver from the audit parameters map
     * @return A configured TanaguruDriver
     */
    Optional<RemoteWebDriver> create(BrowserName webdriverBrowser);
}
