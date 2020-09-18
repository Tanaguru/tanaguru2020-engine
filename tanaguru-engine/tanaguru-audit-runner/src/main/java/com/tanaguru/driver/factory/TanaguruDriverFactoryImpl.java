package com.tanaguru.driver.factory;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Primary
public class TanaguruDriverFactoryImpl implements TanaguruDriverFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(TanaguruDriverFactoryImpl.class);

    @Value("${auditrunner.firefox-binary}")
    private String firefoxBinaryPath;

    @Value("${auditrunner.geckodriver}")
    private String geckodriver;

    @Value("${auditrunner.implicitlyWait}")
    private long implicitlyWait;

    @Value("${auditrunner.pageLoadTimeout}")
    private long pageLoadTimeout;

    @Value("${auditrunner.scriptTimeout}")
    private long scriptTimeout;

    @Value("${auditrunner.firefox.profile}")
    private String firefoxProfilePath;

    @Value("${auditrunner.proxy.host}")
    private String proxyHost;

    @Value("${auditrunner.proxy.port}")
    private String proxyPort;

    @Value("${auditrunner.proxy.username}")
    private String proxyUsername;

    @Value("${auditrunner.proxy.password}")
    private String proxyPassword;

    @Value("${auditrunner.proxy.exclusionUrls}")
    private String proxyExclusionUrls;

    @PostConstruct
    public void setEnv() {
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
        System.setProperty("webdriver.gecko.driver", geckodriver);
    }

    @Override
    public Optional<RemoteWebDriver> create() {
        FirefoxOptions options = new FirefoxOptions();
        FirefoxProfile firefoxProfile = createFirefoxProfile();

        options.setBinary(firefoxBinaryPath);
        options.setHeadless(true);
        options.setProfile(firefoxProfile);

        FirefoxDriver firefoxDriver = null;

        try{
            firefoxDriver = new FirefoxDriver(options);
            firefoxDriver.manage().timeouts().implicitlyWait(implicitlyWait, TimeUnit.SECONDS);
            firefoxDriver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
            firefoxDriver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);
        }catch (IllegalStateException e){
            LOGGER.error("[Webdriver] Could not create webdriver with error :\n" + e.getMessage());
        }

        return Optional.ofNullable(firefoxDriver);
    }



    private FirefoxProfile createFirefoxProfile() {
        LOGGER.trace("Create firefox profile");
        File firefoxProfileFile = new File(firefoxProfilePath);
        FirefoxProfile firefoxProfile = firefoxProfileFile.exists() ?
                new FirefoxProfile(firefoxProfileFile) :
                new FirefoxProfile();
        firefoxProfile.setPreference("browser.startup.page", 0);
        firefoxProfile.setPreference("browser.cache.disk.capacity", 0);
        firefoxProfile.setPreference("browser.cache.disk.enable", false);
        firefoxProfile.setPreference("browser.cache.disk.smart_size.enabled", false);
        firefoxProfile.setPreference("browser.cache.disk.smart_size.first_run", false);
        firefoxProfile.setPreference("browser.cache.disk.smart_size_cached_value", 0);
        firefoxProfile.setPreference("browser.cache.memory.enable", false);
        firefoxProfile.setPreference("browser.shell.checkDefaultBrowser", false);
        firefoxProfile.setPreference("browser.startup.homepage_override.mstone", "ignore");
        firefoxProfile.setPreference("browser.preferences.advanced.selectedTabIndex", 0);
        firefoxProfile.setPreference("browser.privatebrowsing.autostart", false);
        firefoxProfile.setPreference("browser.link.open_newwindow", 2);
        firefoxProfile.setPreference("Network.cookie.cookieBehavior", 1);
        firefoxProfile.setPreference("signon.autologin.proxy", true);

        firefoxProfile.setPreference("signon.autologin.proxy", true);

        // to disable the update of search engines
        firefoxProfile.setPreference("browser.search.update", false);
        firefoxProfile.setAcceptUntrustedCertificates(true);

        setUpProxy(firefoxProfile);

        return firefoxProfile;
    }

    private void setUpProxy(FirefoxProfile firefoxProfile) {
        if (!proxyPort.isEmpty() && !proxyHost.isEmpty()) {
            if (!proxyUsername.isEmpty() && !proxyPassword.isEmpty()) {
                firefoxProfile.setPreference("network.proxy.username", proxyUsername);
                firefoxProfile.setPreference("network.proxy.password", proxyPassword);
            }

            if(!proxyExclusionUrls.isEmpty()){
                firefoxProfile.setPreference("network.proxy.no_proxies_on", proxyExclusionUrls);
            }

            firefoxProfile.setPreference("network.proxy.http", proxyHost);
            firefoxProfile.setPreference("network.proxy.http_port", proxyPort);
            firefoxProfile.setPreference("network.proxy.ssl", proxyHost);
            firefoxProfile.setPreference("network.proxy.ssl_port", proxyPort);
        }
    }
}
