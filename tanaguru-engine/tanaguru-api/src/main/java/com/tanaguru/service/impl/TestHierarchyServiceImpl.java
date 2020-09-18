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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
@Transactional
public class TestHierarchyServiceImpl implements TestHierarchyService {

    private final TestHierarchyRepository testHierarchyRepository;
    private final AuditReferenceRepository auditReferenceRepository;
    private final TanaguruTestRepository tanaguruTestRepository;

    @Value("references/wcag-definition.json")
    private ClassPathResource wcagResource;

    @Value("tests/act.json")
    private ClassPathResource actTestsPath;

    @Autowired
    public TestHierarchyServiceImpl(TestHierarchyRepository testHierarchyRepository, AuditReferenceRepository auditReferenceRepository, TanaguruTestRepository tanaguruTestRepository){
        this.testHierarchyRepository = testHierarchyRepository;
        this.auditReferenceRepository = auditReferenceRepository;
        this.tanaguruTestRepository = tanaguruTestRepository;
    }

    @PostConstruct
    public void insertBaseTestHierarchy() throws IOException {
        if(testHierarchyRepository.count() == 0){
            Gson gson = new Gson();
            JsonTestHierarchy wcag = gson.fromJson(
                    StreamUtils.copyToString(
                            wcagResource.getInputStream(),
                            Charset.defaultCharset()),
                    JsonTestHierarchy.class);
            TestHierarchy actRef = importTestHierarchy(wcag, null);

            String actJson = StreamUtils.copyToString(
                    actTestsPath.getInputStream(),
                    Charset.defaultCharset());

            for(JsonTanaguruWebextTest webextTest :  gson.fromJson(actJson, JsonTanaguruWebextTest[].class)){
                for(String referenceName : webextTest.getRessources().keySet()){
                    Optional<TestHierarchy> referenceOpt = testHierarchyRepository.findByCodeAndParentIsNull(referenceName);
                    if(referenceOpt.isPresent()){
                        for(String ruleCode : webextTest.getRessources().get(referenceName)){
                            Optional<TestHierarchy> testHierarchyOpt = testHierarchyRepository.findByCodeAndReference(ruleCode, referenceOpt.get());
                            if(testHierarchyOpt.isPresent()){
                                TanaguruTest test = new TanaguruTest();
                                Collection<TestHierarchy> testHierarchies = new ArrayList<>();
                                testHierarchies.add(testHierarchyOpt.get());
                                test.setTestHierarchies(testHierarchies);

                                test.setName(webextTest.getName());
                                test.setQuery(webextTest.getQuery());
                                test.setExpectedNbElements(webextTest.getExpectedNbElements());
                                test.setTags(webextTest.getTags());
                                test.setAnalyzeElements(webextTest.getAnalyzeElements());
                                test.setDescription(webextTest.getDescription());
                                test.setFilter(webextTest.getFilter());
                                tanaguruTestRepository.save(test);
                            }
                        }
                    }

                }
            }
        }
    }

    private TestHierarchy importTestHierarchy(JsonTestHierarchy jsonTestHierarchy, TestHierarchy parent){
        TestHierarchy testHierarchy = new TestHierarchy();
        testHierarchy.setName( jsonTestHierarchy.getName());
        testHierarchy.setRank(jsonTestHierarchy.getRank());
        testHierarchy.setUrls(jsonTestHierarchy.getUrls());
        testHierarchy.setCode(jsonTestHierarchy.getCode());
        testHierarchy.setParent(parent);

        if(parent == null){
            testHierarchy.setReference(testHierarchy);
        }else{
            testHierarchy.setReference(parent.getReference());
        }

        testHierarchy = testHierarchyRepository.save(testHierarchy);

        Collection<TestHierarchy> children = new ArrayList<>();
        for(JsonTestHierarchy childJson : jsonTestHierarchy.getChildren()){
            children.add(importTestHierarchy(childJson, testHierarchy));
        }
        testHierarchy.setChildren(children);
        return testHierarchy;
    }

    public void deleteReference(TestHierarchy reference){
        if(auditReferenceRepository.existsByTestHierarchy(reference)){
            testHierarchyRepository.delete(reference);
        }else{
            tagDeletedWithChild(reference);
        }
    }

    public void tagDeletedWithChild(TestHierarchy testHierarchy){
        testHierarchy.setDeleted(true);
        for(TestHierarchy child : testHierarchy.getChildren()){
            tagDeletedWithChild(child);
        }
        testHierarchyRepository.save(testHierarchy);
    }

}
