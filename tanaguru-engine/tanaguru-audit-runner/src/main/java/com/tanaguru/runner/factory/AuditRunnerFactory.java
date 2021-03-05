package com.tanaguru.runner.factory;

import com.tanaguru.domain.constant.BrowserName;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.runner.AuditRunner;
import org.openqa.selenium.Dimension;

import java.util.Collection;
import java.util.Optional;

/**
 * @author rcharre
 */
public interface AuditRunnerFactory {
    /**
     * Create an @see AuditRunner from an @see Audit
     *
     * @param audit The given @see Audit
     * @return An @see AuditRunner
     */
    Optional<AuditRunner> create(Audit audit);

    /**
     * Create an @see AuditRunner from an @see Audit and an url list
     *
     * @param audit             The given audit
     * @param urls              The given urls list
     * @param waitTime          The time to wait before audit start on a page
     * @param resolutions       The breakpoints to audits
     * @param basicAuthUrl      Basic authentication url
     * @param basicAuthLogin    Basic authentication login
     * @param basicAuthPassword Basic authentication password
     * @param enableScreeShot   True to enable webdriver to take screenshot
     * @param webdriverType		The given driver type browser
     * @return An @see AuditRunner
     */
    Optional<AuditRunner> createPageRunner(Collection<TanaguruTest> references, Audit audit, Collection<String> urls, long waitTime, Collection<Integer> resolutions, String basicAuthUrl, String basicAuthLogin, String basicAuthPassword, boolean enableScreeShot, BrowserName browserName);

    /**
     * Create an @see AuditRunner from an @see Audit and an seeds list
     *
     * @param audit             The given audit
     * @param seeds             The given seeds list
     * @param waitTime          The time to wait before audit start on a page
     * @param resolutions       The breakpoints to audits
     * @param basicAuthUrl      Basic authentication url
     * @param basicAuthLogin    Basic authentication login
     * @param basicAuthPassword Basic authentication password
     * @param enableScreeShot   True to enable webdriver to take screenshot
     * @return An @see AuditRunner
     */
    Optional<AuditRunner> createSiteRunner(Collection<TanaguruTest> references, Audit audit, Collection<String> seeds, long waitTime, Collection<Integer> resolutions, String basicAuthUrl, String basicAuthLogin, String basicAuthPassword, boolean enableScreeShot, BrowserName browserName);

    /**
     * Create an @see AuditRunner from an @see Audit and a selenese scenario
     *
     * @param audit             The given audit
     * @param scenario          The given scenario
     * @param waitTime          The time to wait before audit start on a page
     * @param resolutions       The breakpoints to audits
     * @param basicAuthUrl      Basic authentication url
     * @param basicAuthLogin    Basic authentication login
     * @param basicAuthPassword Basic authentication password
     * @param enableScreeShot   True to enable webdriver to take screenshot
     * @return An @see AuditRunner
     */
    Optional<AuditRunner> createSeleneseRunner(Collection<TanaguruTest> references, Audit audit, String scenario, long waitTime, Collection<Integer> resolutions, String basicAuthUrl, String basicAuthLogin, String basicAuthPassword, boolean enableScreeShot, BrowserName browserName);

    /**
     * Create an @see AuditRunner from an Audit and html page
     *
     * @param audit             The given audit
     * @param content           The page content
     * @param waitTime          The time to wait before audit start on a page
     * @param resolutions       The breakpoints to audits
     * @param basicAuthUrl      Basic authentication url
     * @param basicAuthLogin    Basic authentication login
     * @param basicAuthPassword Basic authentication password
     * @param enableScreeShot   True to enable webdriver to take screenshot
     * @return An @see AuditRunner
     */
    Optional<AuditRunner> createFileRunner(Collection<TanaguruTest> references, Audit audit, Collection<String> fileContents, long waitTime, Collection<Integer> resolutions, String basicAuthUrl, String basicAuthLogin, String basicAuthPassword, boolean enableScreeShot, BrowserName browserName);
}
