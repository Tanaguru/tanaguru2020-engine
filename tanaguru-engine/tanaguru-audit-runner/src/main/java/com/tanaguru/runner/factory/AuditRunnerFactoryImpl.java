package com.tanaguru.runner.factory;

import com.tanaguru.config.PropertyConfig;
import com.tanaguru.crawler.TanaguruCrawlerController;
import com.tanaguru.crawler.factory.TanaguruCrawlerControllerFactory;
import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.BrowserName;
import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.entity.audit.*;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.driver.factory.TanaguruDriverFactory;
import com.tanaguru.helper.AESEncrypt;
import com.tanaguru.repository.AuditReferenceRepository;
import com.tanaguru.repository.ResourceRepository;
import com.tanaguru.repository.ScenarioRepository;
import com.tanaguru.repository.TanaguruTestRepository;
import com.tanaguru.runner.*;
import com.tanaguru.service.AuditService;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Component
public class AuditRunnerFactoryImpl implements AuditRunnerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRunnerFactoryImpl.class);

    private final TanaguruDriverFactory tanaguruDriverFactory;
    private final TanaguruCrawlerControllerFactory tanaguruCrawlerControllerFactory;
    private final AuditService auditService;
    private final ScenarioRepository scenarioRepository;
    private final ResourceRepository resourceRepository;
    private final TanaguruTestRepository tanaguruTestRepository;
    private final AuditReferenceRepository auditReferenceRepository;
    
    private final String coreScript;
    private static final String CHROME = "chrome";
    private static final String FIREFOX = "firefox";
    private String cssQuery;

    @Autowired
    public AuditRunnerFactoryImpl(
            TanaguruDriverFactory tanaguruDriverFactory,
            TanaguruCrawlerControllerFactory tanaguruCrawlerControllerFactory,
            AuditService auditService,
            ScenarioRepository scenarioRepository,
            ResourceRepository resourceRepository,
            TanaguruTestRepository tanaguruTestRepository, AuditReferenceRepository auditReferenceRepository, String coreScript) {

        this.tanaguruDriverFactory = tanaguruDriverFactory;
        this.tanaguruCrawlerControllerFactory = tanaguruCrawlerControllerFactory;
        this.auditService = auditService;
        this.scenarioRepository = scenarioRepository;
        this.resourceRepository = resourceRepository;
        this.tanaguruTestRepository = tanaguruTestRepository;
        this.auditReferenceRepository = auditReferenceRepository;
        this.coreScript = coreScript;
    }

    @Override
    public Optional<AuditRunner> create(Audit audit) {
        Optional<AuditRunner> result = Optional.empty();
        Collection<AuditReference> references = auditReferenceRepository.findAllByAudit(audit);
        Collection<TanaguruTest> tanaguruTests = tanaguruTestRepository.findDistinctByTestHierarchies_ReferenceInAndIsDeletedIsFalse(
                references.stream().map(AuditReference::getTestHierarchy)
                        .collect(Collectors.toList()));

        Map<EAuditParameter, AuditParameterValue> parameterStringMap = audit.getParametersAsMap();
        long waitTime = Long.parseLong(parameterStringMap.get(EAuditParameter.WAIT_TIME).getValue());

        String basicAuthUrl = parameterStringMap.get(EAuditParameter.BASICAUTH_URL).getValue();
        String basicAuthLogin = parameterStringMap.get(EAuditParameter.BASICAUTH_LOGIN).getValue();
        String basicAuthPassword = parameterStringMap.get(EAuditParameter.BASICAUTH_PASSWORD).getValue();
        String webdriverBrowser = parameterStringMap.get(EAuditParameter.WEBDRIVER_BROWSER).getValue();
        BrowserName browserName = null;
        switch(webdriverBrowser) {
        	case CHROME:
        		browserName = BrowserName.CHROME;
        		break;
        	case FIREFOX:
        		browserName = BrowserName.FIREFOX;
        		break;
        	default:
        		browserName = BrowserName.FIREFOX;    
        }

        String cssQuery = parameterStringMap.get(EAuditParameter.CSS_QUERY_ON_LOADED_PAGE).getValue();
        
        boolean enableScreenShot = Boolean.parseBoolean(parameterStringMap.get(EAuditParameter.ENABLE_SCREENSHOT).getValue());

        basicAuthLogin =
                basicAuthLogin.isEmpty() ?
                        basicAuthLogin :
                        AESEncrypt.decrypt(basicAuthLogin, PropertyConfig.cryptoKey);
        basicAuthPassword =
                basicAuthPassword.isEmpty() ?
                    basicAuthPassword :
                    AESEncrypt.decrypt(basicAuthPassword, PropertyConfig.cryptoKey);

        Collection<Integer> resolutions =
                Arrays.stream(parameterStringMap.get(EAuditParameter.WEBDRIVER_RESOLUTIONS).getValue()
                .split(";"))
                .map(Integer::parseInt).collect(Collectors.toList());

        switch (audit.getType()) {
            case PAGE:
                Collection<String> urls = new ArrayList<>(
                        Arrays.asList(parameterStringMap.get(EAuditParameter.PAGE_URLS).getValue().split(";"))
                );

                result = createPageRunner(
                        tanaguruTests,
                        audit,
                        urls,
                        waitTime,
                        resolutions,
                        basicAuthUrl,
                        basicAuthLogin,
                        basicAuthPassword,
                        enableScreenShot,
                        browserName,
                        cssQuery);
                break;

            case SITE:
                Collection<String> seeds = new ArrayList<>(
                        Arrays.asList(parameterStringMap.get(EAuditParameter.SITE_SEEDS).getValue().split(";"))
                );
                result = createSiteRunner(
                        tanaguruTests,
                        audit,
                        seeds,
                        waitTime,
                        resolutions,
                        basicAuthUrl,
                        basicAuthLogin,
                        basicAuthPassword,
                        enableScreenShot,
                        browserName,
                        cssQuery);
                break;

            case SCENARIO:
                long scenarioId = Long.parseLong(parameterStringMap.get(EAuditParameter.SCENARIO_ID).getValue());
                Scenario scenario = scenarioRepository.findById(scenarioId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.SCENARIO_NOT_FOUND, scenarioId ));

                result = createSeleneseRunner(
                        tanaguruTests,
                        audit,
                        new String(Base64.getDecoder().decode(scenario.getContent())),
                        waitTime,
                        resolutions,
                        basicAuthUrl,
                        basicAuthLogin,
                        basicAuthPassword,
                        enableScreenShot,
                        browserName,
                        cssQuery);
                break;
            case UPLOAD:
                long resourceId = Long.parseLong(parameterStringMap.get(EAuditParameter.DOM_ID).getValue());
                Resource resource = resourceRepository.findById(resourceId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.RESOURCE_NOT_FOUND, resourceId ));
                result = createFileRunner(
                        tanaguruTests,
                        audit,
                        resource.getContent(),
                        waitTime,
                        resolutions,
                        basicAuthUrl,
                        basicAuthLogin,
                        basicAuthPassword,
                        enableScreenShot,
                        browserName,
                        cssQuery);
                break;
            default:
                auditService.log(audit, EAuditLogLevel.ERROR, audit.getType() + " audit type not handled");
                LOGGER.error("[Audit {}] Audit type not handled", audit.getId());
        }
        return result;
    }


    public Optional<AuditRunner> createPageRunner(
            Collection<TanaguruTest> tanaguruTests,
            Audit audit, Collection<String> urls,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot,
            BrowserName browserName,
            String cssQuery) {
        Optional<AuditRunner> result = Optional.empty();
        Optional<RemoteWebDriver> tanaguruDriver = tanaguruDriverFactory.create(browserName);

        if (tanaguruDriver.isPresent()) {
            result = Optional.of(new AuditRunnerPage(
                    tanaguruTests,
                    audit,
                    urls,
                    tanaguruDriver.get(),
                    coreScript,
                    waitTime,
                    resolutions,
                    basicAuthUrl,
                    basicAuthLogin,
                    basicAuthPassword,
                    enableScreenShot,
                    cssQuery)
            );
        } else {
            auditService.log(audit, EAuditLogLevel.ERROR, "Unable to create page audit runner");
            LOGGER.error("[Audit {}] Unable to create page audit runner", audit.getId());
        }
        return result;
    }

    public Optional<AuditRunner> createSeleneseRunner(
            Collection<TanaguruTest> tanaguruTests,
            Audit audit,
            String scenario,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot,
            BrowserName browserName,
            String cssQuery) {
        Optional<AuditRunner> result = Optional.empty();
        Optional<RemoteWebDriver> tanaguruDriver = tanaguruDriverFactory.create(browserName);

        if (tanaguruDriver.isPresent()) {
            result = Optional.of(new AuditRunnerSelenese(
                    tanaguruTests,
                    audit,
                    scenario,
                    tanaguruDriver.get(),
                    coreScript,
                    waitTime,
                    resolutions,
                    basicAuthUrl,
                    basicAuthLogin,
                    basicAuthPassword,
                    enableScreenShot,
                    cssQuery)
            );
        } else {
            auditService.log(audit, EAuditLogLevel.ERROR, "Unable to create scenario audit runner");
            LOGGER.error("[Audit {}] Unable to create selenese audit runner", audit.getId());
        }
        return result;
    }

    public Optional<AuditRunner> createSiteRunner(
            Collection<TanaguruTest> tanaguruTests,
            Audit audit,
            Collection<String> seeds,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot,
            BrowserName browserName,
            String cssQuery) {
        Optional<AuditRunner> result = Optional.empty();
        Map<EAuditParameter, AuditParameterValue> auditParameterValueMap = audit.getParametersAsMap();
        Optional<RemoteWebDriver> tanaguruDriver = tanaguruDriverFactory.create(browserName);
        Optional<TanaguruCrawlerController> tanaguruCrawlerController = tanaguruCrawlerControllerFactory.create(
                seeds,
                Long.parseLong(auditParameterValueMap.get(EAuditParameter.CRAWLER_MAX_DURATION).getValue()),
                auditParameterValueMap.get(EAuditParameter.CRAWLER_INCLUSION_REGEX).getValue(),
                auditParameterValueMap.get(EAuditParameter.CRAWLER_EXCLUSION_REGEX).getValue(),
                Integer.parseInt(auditParameterValueMap.get(EAuditParameter.CRAWLER_MAX_DOCUMENT).getValue()),
                Integer.parseInt(auditParameterValueMap.get(EAuditParameter.CRAWLER_MAX_DEPTH).getValue()),
                basicAuthUrl,
                basicAuthLogin,
                basicAuthPassword
        );

        if (tanaguruDriver.isPresent() && tanaguruCrawlerController.isPresent()) {
            result = Optional.of(new AuditRunnerSite(
                    tanaguruTests,
                    audit,
                    tanaguruCrawlerController.get(),
                    tanaguruDriver.get(),
                    coreScript,
                    waitTime,
                    resolutions,
                    basicAuthUrl,
                    basicAuthLogin,
                    basicAuthPassword,
                    enableScreenShot,
                    cssQuery)
            );
        } else {
            auditService.log(audit, EAuditLogLevel.ERROR, "Unable to create site audit runner");
            LOGGER.error("[Audit {}] Unable to create site audit runner", audit.getId());
        }

        return result;
    }

    public Optional<AuditRunner> createFileRunner(
            Collection<TanaguruTest> tanaguruTests,
            Audit audit,
            String content,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot,
            BrowserName browserName,
            String cssQuery) {
        Optional<AuditRunner> result = Optional.empty();
        Optional<RemoteWebDriver> tanaguruDriver = tanaguruDriverFactory.create(browserName);

        if (tanaguruDriver.isPresent()) {
            result = Optional.of(new AuditRunnerFile(
                    tanaguruTests,
                    audit,
                    content,
                    tanaguruDriver.get(),
                    coreScript,
                    waitTime,
                    resolutions,
                    basicAuthUrl,
                    basicAuthLogin,
                    basicAuthPassword,
                    enableScreenShot,
                    cssQuery)
            );
        } else {
            auditService.log(audit, EAuditLogLevel.ERROR, "Unable to create file audit runner");
            LOGGER.error("[Audit {}] Unable to create file audit runner", audit.getId());
        }
        return result;
    }
}
