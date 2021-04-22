package service.impl;

import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.constant.EParameterFamily;
import com.tanaguru.domain.entity.audit.Resource;
import com.tanaguru.domain.entity.audit.Scenario;
import com.tanaguru.domain.entity.audit.parameter.AuditParameter;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterFamily;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;
import com.tanaguru.repository.*;
import com.tanaguru.service.ScenarioService;
import com.tanaguru.service.impl.AuditParameterServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static com.tanaguru.domain.constant.ParameterValueConstants.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AuditParameterServiceImplTest {
    @Mock
    private AuditParameterFamilyRepository auditParameterFamilyRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ScenarioRepository scenarioRepository;

    @Mock
    private ScenarioService scenarioService;

    @Mock
    private AuditParameterValueRepository auditParameterValueRepository;

    @Mock
    private AuditParameterRepository auditParameterRepository;

    @InjectMocks
    private AuditParameterServiceImpl auditParameterServiceImpl;

    private List<AuditParameterFamily> auditParameterFamilies = new ArrayList<>();
    private List<AuditParameter> auditParameterForfamily1 = new ArrayList<>();
    private List<AuditParameter> auditParameterForfamily2 = new ArrayList<>();

    @Before
    public void setUp() {
        // Family 1
        AuditParameterFamily auditParameterFamily1 = new AuditParameterFamily();
        auditParameterFamily1.setId(0);
        auditParameterFamily1.setCode(EParameterFamily.WEBDRIVER);
        auditParameterFamilies.add(auditParameterFamily1);

        AuditParameter auditParameter1 = new AuditParameter();
        auditParameter1.setId(0);
        auditParameter1.setCode(EAuditParameter.WEBDRIVER_RESOLUTIONS);
        auditParameter1.setAuditParameterFamily(auditParameterFamily1);
        auditParameterForfamily1.add(auditParameter1);

        AuditParameterValue auditParameterValue1 = new AuditParameterValue();
        auditParameterValue1.setDefault(true);
        auditParameterValue1.setValue("test1");
        auditParameterValue1.setAuditParameter(auditParameter1);

        //Family 2
        AuditParameterFamily auditParameterFamily2 = new AuditParameterFamily();
        auditParameterFamily2.setId(1);
        auditParameterFamily2.setCode(EParameterFamily.CRAWLER);
        auditParameterFamilies.add(auditParameterFamily2);

        AuditParameter auditParameter3 = new AuditParameter();
        auditParameter3.setId(2);
        auditParameter3.setCode(EAuditParameter.CRAWLER_EXCLUSION_REGEX);
        auditParameter3.setAuditParameterFamily(auditParameterFamily1);
        auditParameterForfamily2.add(auditParameter3);

        AuditParameterValue auditParameterValue3 = new AuditParameterValue();
        auditParameterValue3.setDefault(true);
        auditParameterValue3.setValue("test3");
        auditParameterValue3.setAuditParameter(auditParameter3);

        AuditParameter auditParameter4 = new AuditParameter();
        auditParameter4.setId(3);
        auditParameter4.setCode(EAuditParameter.CRAWLER_INCLUSION_REGEX);
        auditParameterForfamily2.add(auditParameter4);

        AuditParameterValue auditParameterValue4 = new AuditParameterValue();
        auditParameterValue4.setDefault(true);
        auditParameterValue4.setValue("test4");
        auditParameterValue4.setAuditParameter(auditParameter4);

        Mockito.when(auditParameterFamilyRepository.findAll())
                .thenReturn(auditParameterFamilies);

        Mockito.when(auditParameterRepository.findByAuditParameterFamily(auditParameterFamily1))
                .thenReturn(auditParameterForfamily1);

        Mockito.when(auditParameterRepository.findByAuditParameterFamily(auditParameterFamily2))
                .thenReturn(auditParameterForfamily2);

        auditParameterServiceImpl.initParameterMaps();
    }

    @Test
    public void getDefaultParameterForAuditTypeTest() {
        Map<AuditParameter, AuditParameterValue> siteParameterMap =
                auditParameterServiceImpl.getDefaultParameterForAuditType(EAuditType.SITE);

        Map<AuditParameter, AuditParameterValue> pageParameterMap =
                auditParameterServiceImpl.getDefaultParameterForAuditType(EAuditType.PAGE);

        assertTrue(siteParameterMap.keySet().containsAll(auditParameterForfamily1));
        assertTrue(siteParameterMap.keySet().containsAll(auditParameterForfamily2));

        assertTrue(pageParameterMap.keySet().containsAll(auditParameterForfamily1));
    }

    @Test
    public void checkParameterValueIsValid_waitTimeInvalidFormat() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WAIT_TIME, "test", null));
    }

    @Test
    public void checkParameterValueIsValid_waitTimeTooHight() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WAIT_TIME, "" + (MAX_WAIT_TIME + 1), null));
    }

    @Test
    public void checkParameterValueIsValid_waitTimeTooLow() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WAIT_TIME, "-1", null));
    }

    @Test
    public void checkParameterValueIsValid_waitTimeValid() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WAIT_TIME, "" + (MAX_WAIT_TIME - 1), null));
    }

    @Test
    public void checkParameterValueIsValid_DOMIDValid() {
        Mockito.when(resourceRepository.findAllByIdIn(Mockito.anyList()))
                .thenReturn(Arrays.asList(new Resource()));
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.DOM_ID, "0", null));
    }

    @Test
    public void checkParameterValueIsValid_DOMIDValidMultiple() {
        Mockito.when(resourceRepository.findAllByIdIn(Mockito.anyList()))
                .thenReturn(Arrays.asList(new Resource(), new Resource()));
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.DOM_ID, "0;1", null));
    }

    @Test
    public void checkParameterValueIsValid_DOMIDInvalidFormat() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.DOM_ID, "test", null));
    }

    @Test
    public void checkParameterValueIsValid_DOMIDNotExists() {
        Mockito.when(resourceRepository.findAllByIdIn(Mockito.anyList()))
                .thenReturn(Collections.emptyList());
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.DOM_ID, "0", null));
    }

    @Test
    public void checkParameterValueIsValid_ScenarioIDValid() {
        Scenario scenario = new Scenario();

        Mockito.when(scenarioRepository.findById(0L))
                .thenReturn(Optional.of(scenario));

        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.SCENARIO_ID, "0", null));
    }

    @Test
    public void checkParameterValueIsValid_ScenarioIDNotExists() {
        Mockito.when(scenarioRepository.findById(0L))
                .thenReturn(Optional.empty());

        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.SCENARIO_ID, "0", null));
    }

    @Test
    public void checkParameterValueIsValid_ScenarioIDInvalidFormat() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.SCENARIO_ID, "test", null));
    }

    @Test
    public void checkParameterValueIsValid_SiteSeedsValidUnique() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.SITE_SEEDS, "http://tanaguru.com", null));
    }

    @Test
    public void checkParameterValueIsValid_SiteSeedsValidMultiple() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.SITE_SEEDS, "http://tanaguru.com;http://longdesc.fr", null));
    }

    @Test
    public void checkParameterValueIsValid_SiteSeedsInvalidUnique() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.SITE_SEEDS, "tanaguru.com", null));
    }

    @Test
    public void checkParameterValueIsValid_SiteSeedsInvalidMultiple() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.SITE_SEEDS, "tanaguru.com,http://longdesc.fr", null));
    }

    @Test
    public void checkParameterValueIsValid_SiteSeedsInvalidMultiple2() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.SITE_SEEDS, "http://longdesc.fr;tanaguru.com", null));
    }

    @Test
    public void checkParameterValueIsValid_PageurlsValidUnique() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.PAGE_URLS, "http://tanaguru.com", null));
    }

    @Test
    public void checkParameterValueIsValid_PageurlsValidMultiple() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.PAGE_URLS, "http://tanaguru.com;http://longdesc.fr", null));
    }

    @Test
    public void checkParameterValueIsValid_PageurlsValidUniqueIP() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.PAGE_URLS, "http://10.200.192.33/", null));
    }

    @Test
    public void checkParameterValueIsValid_PageurlsValidUniqueIntra() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.PAGE_URLS, "http://git-scm.test.intra/", null));
    }

    @Test
    public void checkParameterValueIsValid_PageurlsInvalidUnique() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.PAGE_URLS, "tanaguru.com", null));
    }

    @Test
    public void checkParameterValueIsValid_PageurlsInvalidMultiple() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.PAGE_URLS, "tanaguru.com;http://longdesc.fr", null));
    }

    @Test
    public void checkParameterValueIsValid_webdriverResolutionsValid() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WEBDRIVER_RESOLUTIONS, "1920", null));
    }



    @Test
    public void checkParameterValueIsValid_webdriverResolutionsValid2() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WEBDRIVER_RESOLUTIONS, "1920;1024", null));
    }

    @Test
    public void checkParameterValueIsValid_webdriverResolutionsInvalidFormat() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WEBDRIVER_RESOLUTIONS, "test", null));
    }

    @Test
    public void checkParameterValueIsValid_webdriverResolutionsInvalidFormat2() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WEBDRIVER_RESOLUTIONS, "1920,1024", null));
    }

    @Test
    public void checkParameterValueIsValid_webdriverResolutionsInvalidFormat3() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WEBDRIVER_RESOLUTIONS, "1920;test", null));
    }

    @Test
    public void checkParameterValueIsValid_webdriverWidthToHigh() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WEBDRIVER_RESOLUTIONS, "" + (MAX_WEBDRIVER_WIDTH + 1), null));
    }

    @Test
    public void checkParameterValueIsValid_webdriverWidthTooLow() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WEBDRIVER_RESOLUTIONS, "-1", null));
    }

    @Test
    public void checkParameterValueIsValid_webdriverWidthValid() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.WEBDRIVER_RESOLUTIONS, "" + (MAX_WEBDRIVER_WIDTH), null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdocumentInvalidFormat() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DOCUMENT, "test", null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdocumentTooHight() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DOCUMENT, "" + (MAX_CRAWLER_DOCUMENTS + 1), null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdocumentTooLow() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DOCUMENT, "0", null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdocumentValid() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DOCUMENT, "" + (MAX_CRAWLER_DOCUMENTS - 1), null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdurationInvalidFormat() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DURATION, "test", null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdurationTooHight() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DURATION, "" + (MAX_CRAWLER_TIME + 1), null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdurationTooLow() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DURATION, "0", null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdurationValid() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DURATION, "" + (MAX_CRAWLER_TIME - 1), null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdepthInvalidFormat() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DEPTH, "test", null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdepthTooHight() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DEPTH, "" + (MAX_CRAWLER_DEPTH + 1), null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdepthTooLow() {
        assertFalse(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DEPTH, "0", null));
    }

    @Test
    public void checkParameterValueIsValid_crawlermaxdepthValid() {
        assertTrue(auditParameterServiceImpl.checkParameterValueIsValid(EAuditParameter.CRAWLER_MAX_DEPTH, "" + (MAX_CRAWLER_DEPTH - 1), null));
    }
}
