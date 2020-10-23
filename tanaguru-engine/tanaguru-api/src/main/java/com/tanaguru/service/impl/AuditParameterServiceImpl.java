package com.tanaguru.service.impl;

import com.tanaguru.config.PropertyConfig;
import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.constant.EParameterFamily;
import com.tanaguru.domain.entity.audit.parameter.AuditParameter;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterFamily;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.exception.InvalidEntityException;
import com.tanaguru.helper.AESEncrypt;
import com.tanaguru.helper.UrlHelper;
import com.tanaguru.repository.*;
import com.tanaguru.service.AuditParameterService;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

import static com.tanaguru.domain.constant.EAuditType.*;
import static com.tanaguru.domain.constant.ParameterValueConstants.*;

/**
 * @author rcharre
 */
@Service
public class AuditParameterServiceImpl implements AuditParameterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditParameterServiceImpl.class);

    private static final Collection<EParameterFamily> SITE_AUDIT_ENUM_PARAMETER_FAMILIES = List.of(
            EParameterFamily.SITE,
            EParameterFamily.CRAWLER,
            EParameterFamily.WEBDRIVER,
            EParameterFamily.RULE,
            EParameterFamily.GENERAL
    );
    private static final Collection<EParameterFamily> PAGE_AUDIT_ENUM_PARAMETER_FAMILIES = List.of(
            EParameterFamily.PAGE,
            EParameterFamily.WEBDRIVER,
            EParameterFamily.RULE,
            EParameterFamily.GENERAL
    );
    private static final Collection<EParameterFamily> SCENARIO_AUDIT_ENUM_PARAMETER_FAMILIES = List.of(
            EParameterFamily.SCENARIO,
            EParameterFamily.WEBDRIVER,
            EParameterFamily.RULE,
            EParameterFamily.GENERAL
    );
    private static final Collection<EParameterFamily> UPLOAD_AUDIT_ENUM_PARAMETER_FAMILIES = List.of(
            EParameterFamily.UPLOAD,
            EParameterFamily.WEBDRIVER,
            EParameterFamily.RULE,
            EParameterFamily.GENERAL
    );

    private Map<EParameterFamily, Collection<AuditParameter>> auditParametersByFamilyName;
    private Map<EParameterFamily, AuditParameterFamily> auditParameterFamiliesMap;
    private Map<EAuditType, Collection<EParameterFamily>> auditParameterFamiliesByAuditTypeMap;
    private Map<EAuditParameter, AuditParameter> auditParameterMap;
    private Map<EAuditParameter, AuditParameterValue> auditParameterDefaultValueMap;

    private final AuditParameterFamilyRepository auditParameterFamilyRepository;
    private final AuditParameterValueRepository auditParameterValueRepository;
    private final AuditParameterRepository auditParameterRepository;
    private final ScenarioRepository scenarioRepository;
    private final ResourceRepository resourceRepository;

    @Autowired
    public AuditParameterServiceImpl(
            AuditParameterFamilyRepository auditParameterFamilyRepository,
            AuditParameterValueRepository auditParameterValueRepository,
            AuditParameterRepository auditParameterRepository, ScenarioRepository scenarioRepository, ResourceRepository resourceRepository) {

        this.auditParameterFamilyRepository = auditParameterFamilyRepository;
        this.auditParameterValueRepository = auditParameterValueRepository;
        this.auditParameterRepository = auditParameterRepository;
        this.scenarioRepository = scenarioRepository;
        this.resourceRepository = resourceRepository;
    }

    @PostConstruct
    public void initParameterMaps() {
        auditParametersByFamilyName = new EnumMap<>(EParameterFamily.class);
        auditParameterFamiliesMap = new EnumMap<>(EParameterFamily.class);
        auditParameterFamiliesByAuditTypeMap = new EnumMap<>(EAuditType.class);
        auditParameterMap = new EnumMap<>(EAuditParameter.class);
        auditParameterDefaultValueMap = new EnumMap<>(EAuditParameter.class);

        Collection<AuditParameterFamily> families = auditParameterFamilyRepository.findAll();
        for (AuditParameterFamily family : families) {
            this.auditParameterFamiliesMap.put(family.getCode(), family);

            this.auditParametersByFamilyName.put(
                    family.getCode(),
                    auditParameterRepository.findByAuditParameterFamily(family)
            );
        }

        auditParameterFamiliesByAuditTypeMap.put(SITE, SITE_AUDIT_ENUM_PARAMETER_FAMILIES);
        auditParameterFamiliesByAuditTypeMap.put(SCENARIO, SCENARIO_AUDIT_ENUM_PARAMETER_FAMILIES);
        auditParameterFamiliesByAuditTypeMap.put(UPLOAD, UPLOAD_AUDIT_ENUM_PARAMETER_FAMILIES);
        auditParameterFamiliesByAuditTypeMap.put(PAGE, PAGE_AUDIT_ENUM_PARAMETER_FAMILIES);


        for (AuditParameter auditParameter : auditParameterRepository.findAll()) {
            auditParameterMap.put(auditParameter.getCode(), auditParameter);
            auditParameterDefaultValueMap.put(
                    auditParameter.getCode(),
                    auditParameterValueRepository.findFirstByIsDefaultAndAuditParameter(true, auditParameter)
                            .orElseThrow(() -> new InvalidEntityException("Audit parameter " + auditParameter.getCode() + "has not default value"))
            );
        }
    }

    public Map<EParameterFamily, Collection<AuditParameter>> getAuditParametersByFamilyName() {
        return auditParametersByFamilyName;
    }

    public Map<EParameterFamily, AuditParameterFamily> getAuditParameterFamiliesMap() {
        return auditParameterFamiliesMap;
    }


    public Collection<AuditParameter> getAuditParametersForFamily(EParameterFamily parameterFamily) {
        return auditParametersByFamilyName.containsKey(parameterFamily) ?
                this.auditParametersByFamilyName.get(parameterFamily) :
                Collections.emptyList();
    }

    public Map<AuditParameter, AuditParameterValue> getDefaultParameterForAuditType(EAuditType auditType) {
        Collection<AuditParameter> auditParameters = new ArrayList<>();

        for (EParameterFamily parameterFamily : auditParameterFamiliesByAuditTypeMap.get(auditType)) {
            auditParameters.addAll(
                    getAuditParametersForFamily(parameterFamily)
            );
        }

        Map<AuditParameter, AuditParameterValue> resultMap = new HashMap<>();
        for (AuditParameter parameter : auditParameters) {
            resultMap.put(
                    parameter,
                    auditParameterDefaultValueMap.get(parameter.getCode())
            );
        }
        return resultMap;
    }

    public Map<AuditParameter, AuditParameterValue> getParameterMapForAuditTypeWithParameterOverride(EAuditType auditType, Map<EAuditParameter, String> override, Project project) {
        Map<AuditParameter, AuditParameterValue> defaultParameters = getDefaultParameterForAuditType(auditType);
        Map<AuditParameter, AuditParameterValue> definiteParameters = new HashMap<>();
        for (AuditParameter parameter : defaultParameters.keySet()) {
            String value;

            if (override.containsKey(parameter.getCode())) {
                value = override.get(parameter.getCode());
                //Check if value must be encrypted
                if(parameter.getCode() == EAuditParameter.BASICAUTH_LOGIN && !value.isEmpty() ||
                        parameter.getCode() == EAuditParameter.BASICAUTH_PASSWORD && !value.isEmpty()){
                    value = AESEncrypt.encrypt(value, PropertyConfig.cryptoKey);
                }
            } else {
                value = defaultParameters.get(parameter).getValue();
            }

            // Default value can be invalid, for example SITE_URLS is empty by default
            if (checkParameterValueIsValid(parameter.getCode(), value, project)){
                // Save value if not already persisted
                String finalValue = value;
                AuditParameterValue existingParameterValueOpt = auditParameterValueRepository.findByAuditParameter_CodeAndValue(parameter.getCode(), value)
                        .orElseGet(() -> auditParameterValueRepository.save(
                                new AuditParameterValue(parameter, finalValue, false)));

                definiteParameters.put(parameter, existingParameterValueOpt);
            } else {
                throw new InvalidEntityException("Invalid value for parameter " + parameter.getCode() + " : " + value);
            }
        }

        return definiteParameters;
    }

    public boolean checkParameterValueIsValid(EAuditParameter parameter, String value, Project project) {
        boolean result = false;
        try {
            switch (parameter) {
                case WAIT_TIME:
                    long waitTime = Long.parseLong(value);
                    result = waitTime <= MAX_WAIT_TIME && waitTime >= 0;
                    break;

                case DOM_ID:
                    long resourceId = Long.parseLong(value);
                    result = resourceRepository.existsById(resourceId);
                    break;

                case SCENARIO_ID:
                    long scenarioId = Long.parseLong(value);
                    result = scenarioRepository.findById(scenarioId).isPresent();
                    break;
                case BASICAUTH_URL:
                    result = value.isEmpty() || UrlHelper.isValid(value);
                    break;
                case SITE_SEEDS:
                case PAGE_URLS:
                    String[] urls = value.split(";");
                    if(urls.length > 0) {
                        result = Arrays.stream(urls).allMatch((url) ->{
                            boolean match = UrlHelper.isValid(url);
                            if(project != null &&!project.getContract().isRestrictDomain() && project.getDomain() != null && !project.getDomain().isEmpty()){
                                WebURL sourceDomain = new WebURL();
                                sourceDomain.setURL(project.getDomain());
                                WebURL target = new WebURL();
                                target.setURL(url);
                                match &= target.getDomain().equals(sourceDomain.getDomain());
                            }
                            return match;
                        });
                    }
                    break;

                case WEBDRIVER_RESOLUTIONS:
                    String[] resolutions = value.split(";");
                    result = Arrays.stream(resolutions).allMatch((resolution) -> {
                        int webdriverWidth = Integer.parseInt(resolution);
                        return webdriverWidth <= MAX_WEBDRIVER_WIDTH && webdriverWidth > 0;
                    });
                break;

                case CRAWLER_MAX_DOCUMENT:
                    long crawlerMaxDocument = Long.parseLong(value);
                    result = crawlerMaxDocument <= MAX_CRAWLER_DOCUMENTS && crawlerMaxDocument > 0;
                    break;

                case CRAWLER_MAX_DURATION:
                    long crawlerMaxDuration = Long.parseLong(value);
                    result = crawlerMaxDuration <= MAX_CRAWLER_TIME && crawlerMaxDuration > 0;
                    break;

                case CRAWLER_MAX_DEPTH:
                    long crawlerMaxDepth = Long.parseLong(value);
                    result = crawlerMaxDepth <= MAX_CRAWLER_DEPTH && crawlerMaxDepth > 0;
                    break;

                case ENABLE_SCREENSHOT:
                    boolean enableScreenshot = Boolean.parseBoolean(value);
                    result = true;

                default:
                    result = true;
            }

        } catch (RuntimeException e) {
            LOGGER.error("Unable to parse parameter {} with value {}", parameter, value);
        }

        return result;
    }
}
