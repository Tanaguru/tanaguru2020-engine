package com.tanaguru.service.impl;

import com.google.gson.Gson;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.jsonmapper.JsonTanaguruWebextTest;
import com.tanaguru.domain.jsonmapper.JsonTestHierarchy;
import com.tanaguru.repository.AuditReferenceRepository;
import com.tanaguru.repository.TanaguruTestRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.service.TestHierarchyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

@Service
@Transactional
public class TestHierarchyServiceImpl implements TestHierarchyService {
    private final Logger LOGGER = LoggerFactory.getLogger(TestHierarchyServiceImpl.class);

    private final TestHierarchyRepository testHierarchyRepository;
    private final AuditReferenceRepository auditReferenceRepository;
    private final TanaguruTestRepository tanaguruTestRepository;

    @Value("references/wcag-definition.json")
    private ClassPathResource wcagResource;

    @Value("tests/act.json")
    private ClassPathResource actTestsPath;

    @Autowired
    public TestHierarchyServiceImpl(TestHierarchyRepository testHierarchyRepository, AuditReferenceRepository auditReferenceRepository, TanaguruTestRepository tanaguruTestRepository) {
        this.testHierarchyRepository = testHierarchyRepository;
        this.auditReferenceRepository = auditReferenceRepository;
        this.tanaguruTestRepository = tanaguruTestRepository;
    }

    @PostConstruct
    @Transactional
    public void insertBaseTestHierarchy() throws IOException {
        Gson gson = new Gson();
        JsonTestHierarchy wcag = gson.fromJson(
                StreamUtils.copyToString(
                        wcagResource.getInputStream(),
                        Charset.defaultCharset()),
                JsonTestHierarchy.class);

        if (!testHierarchyRepository.findByCodeAndParentIsNull(wcag.getCode()).isPresent()) {
            LOGGER.info("Create reference " + wcag.getCode());
            TestHierarchy actRef = importTestHierarchy(wcag, null);
            String actJson = StreamUtils.copyToString(
                    actTestsPath.getInputStream(),
                    Charset.defaultCharset());

            Map<String, TestHierarchy> referenceByCode = new HashMap<>();
            referenceByCode.put(actRef.getCode(), actRef);

            for (JsonTanaguruWebextTest webextTest : gson.fromJson(actJson, JsonTanaguruWebextTest[].class)) {
                TanaguruTest test = new TanaguruTest();
                test.setName(webextTest.getName());
                test.setQuery(webextTest.getQuery());
                test.setExpectedNbElements(webextTest.getExpectedNbElements());
                test.setTags(webextTest.getTags());
                test.setAnalyzeElements(webextTest.getAnalyzeElements());
                test.setDescription(webextTest.getDescription());
                test.setFilter(webextTest.getFilter());
                test = tanaguruTestRepository.save(test);

                for (String referenceName : webextTest.getRessources().keySet()) {
                    TestHierarchy reference = null;
                    if (!referenceByCode.containsKey(referenceName)) {
                        Optional<TestHierarchy> referenceOpt = testHierarchyRepository.findByCodeAndParentIsNull(referenceName);
                        if (referenceOpt.isPresent()) {
                            TestHierarchy ref = referenceOpt.get();
                            referenceByCode.put(ref.getCode(), ref);
                            reference = ref;
                        }
                    } else {
                        reference = referenceByCode.get(referenceName);
                    }

                    if (reference != null) {
                        for (String ruleCode : webextTest.getRessources().get(referenceName)) {
                            Optional<TestHierarchy> testHierarchyOpt = testHierarchyRepository.findByCodeAndReference(ruleCode, referenceByCode.get(referenceName));
                            if (testHierarchyOpt.isPresent()) {
                                TestHierarchy testHierarchy = testHierarchyOpt.get();
                                Collection<TanaguruTest> tests = new ArrayList<>(testHierarchy.getTanaguruTests());
                                tests.add(test);
                                testHierarchy.setTanaguruTests(tests);
                                testHierarchyRepository.save(testHierarchy);
                            }
                        }
                    }
                }
            }
        }
    }

    private TestHierarchy importTestHierarchy(JsonTestHierarchy jsonTestHierarchy, TestHierarchy parent) {
        TestHierarchy testHierarchy = new TestHierarchy();
        testHierarchy.setName(jsonTestHierarchy.getName());
        testHierarchy.setRank(jsonTestHierarchy.getRank());
        testHierarchy.setUrls(jsonTestHierarchy.getUrls());
        testHierarchy.setCode(jsonTestHierarchy.getCode());
        testHierarchy.setParent(parent);

        if (parent == null) {
            testHierarchy.setReference(testHierarchy);
        } else {
            testHierarchy.setReference(parent.getReference());
        }

        testHierarchy = testHierarchyRepository.save(testHierarchy);

        Collection<TestHierarchy> children = new ArrayList<>();
        for (JsonTestHierarchy childJson : jsonTestHierarchy.getChildren()) {
            children.add(importTestHierarchy(childJson, testHierarchy));
        }
        testHierarchy.setChildren(children);
        return testHierarchy;
    }

    public void deleteReference(TestHierarchy reference) {
        if (auditReferenceRepository.existsByTestHierarchy(reference)) {
            tagDeletedWithChild(reference);

        } else {
            testHierarchyRepository.delete(reference);
        }
    }

    public void tagDeletedWithChild(TestHierarchy testHierarchy) {
        testHierarchy.setDeleted(true);
        for (TestHierarchy child : testHierarchy.getChildren()) {
            tagDeletedWithChild(child);
        }
        testHierarchyRepository.save(testHierarchy);
    }

}
