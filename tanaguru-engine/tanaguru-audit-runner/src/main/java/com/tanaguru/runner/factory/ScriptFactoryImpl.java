package com.tanaguru.runner.factory;

import com.google.gson.Gson;
import com.tanaguru.crawler.factory.TanaguruCrawlerControllerFactory;
import com.tanaguru.domain.entity.audit.AuditReference;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.driver.factory.TanaguruDriverFactory;
import com.tanaguru.repository.AuditReferenceRepository;
import com.tanaguru.repository.ResourceRepository;
import com.tanaguru.repository.ScenarioRepository;
import com.tanaguru.repository.TanaguruTestRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.service.AuditService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Component
public class ScriptFactoryImpl implements ScriptFactory {
    
    /**
     * Create an executable script.
     *
     * @param coreScript       the content script
     * @param tanaguruTestList tanaguru tests
     * @return the full script
     */
    public HashMap<String,HashMap<String, StringBuilder>> create(String coreScript, Collection<TanaguruTest> tanaguruTestList, Collection<AuditReference> references) {
        
        HashMap<String,HashMap<String, StringBuilder>>  scriptsListByReference = new HashMap<String,HashMap<String, StringBuilder>>();     
        for(AuditReference reference : references) {
            HashMap<String, StringBuilder> scriptsByCat = new HashMap<String, StringBuilder>();
            for (TanaguruTest tanaguruTest : tanaguruTestList) {
                String categoryName = null;
                for(TestHierarchy th : tanaguruTest.getTestHierarchies()) {
                    if(th.getReference().getId() == reference.getTestHierarchy().getId()) {
                        categoryName = th.getParent().getParent().getName();
                    } 
                }
                if(categoryName != null) {
                    if(scriptsByCat.containsKey(categoryName)) {
                        StringBuilder script = scriptsByCat.get(categoryName);
                        script.append(createTngTestForScript(tanaguruTest));
                        scriptsByCat.put(categoryName, script);
                    }else {
                        StringBuilder script = new StringBuilder();
                        script.append(coreScript);
                        //script.append("\n");
                        //script.append("getNACat();\n");
                        script.append(createTngTestForScript(tanaguruTest));
                        scriptsByCat.put(categoryName, script);
                    }
                }
                
            }
            scriptsListByReference.put(reference.getTestHierarchy().getName(), scriptsByCat);
        }
        for(String refKey : scriptsListByReference.keySet()) {
            for(String category : scriptsListByReference.get(refKey).keySet()) {
                StringBuilder script = scriptsListByReference.get(refKey).get(category);
                script.append("\nremoveAllDataTNG();");
                script.append("\nreturn JSON.stringify(loadTanaguruTests());");
                scriptsListByReference.get(refKey).put(category, script);
            }
        }
        return scriptsListByReference;
    }
    
    /**
     * 
     * @param tanaguruTest
     * @return
     */
    private StringBuilder createTngTestForScript(TanaguruTest tanaguruTest) {
        Gson gson = new Gson();
        StringBuilder createTngTest = new StringBuilder();
        boolean isFirst = true;
        StringBuilder strbTags = new StringBuilder();
        strbTags.append("[");
        for(String tag : tanaguruTest.getTags()) {
            if(isFirst) {
                strbTags.append("'"+tag+"'");
                isFirst = false;
            }else {
                strbTags.append(",'"+tag+"'");
            }
                
        }
        strbTags.append("]");
        createTngTest.append("\nvar testStatusJS = null;");
        createTngTest.append("\nnaList.forEach(na => {\n"
                + "            if("+strbTags.toString()+".includes(na)) testStatusJS = 'inapplicable';\n"
                + "        }); ");
        createTngTest.append("\nif(testStatusJS == null){\n"
                + "    testStatusJS = '"+ tanaguruTest.getStatus()+"';"
                + "\n}");
        createTngTest.append("\ncreateTanaguruTest({id:").append(tanaguruTest.getId());
        createTngTest.append(",\nname:`").append(tanaguruTest.getName()).append("`");
        if(tanaguruTest.getQuery() != null && tanaguruTest.getQuery().startsWith("HTML")) {
            createTngTest.append(",\nquery:").append(tanaguruTest.getQuery());
        }else if(tanaguruTest.getQuery() != null){
            createTngTest.append(",\nquery:`").append(tanaguruTest.getQuery()).append("`");
        }
        createTngTest.append(",\ntags:").append(gson.toJson(tanaguruTest.getTags()));

        if (tanaguruTest.getExpectedNbElements() != null) {
            createTngTest.append(",\nexpectedNbElements:");
            try {
                createTngTest.append(
                        Integer.parseInt(tanaguruTest.getExpectedNbElements()));
            } catch (NumberFormatException nfe) {
                createTngTest.append(gson.toJson(tanaguruTest.getExpectedNbElements()));
            }
        }

        if (tanaguruTest.getDescription() != null) {
            createTngTest.append(",\ndescription:`").append(tanaguruTest.getDescription()).append("`");
        }

        if (tanaguruTest.getCode() != null) {
            createTngTest.append(",\ncode:`").append(tanaguruTest.getCode()).append("`");
        }

        if (tanaguruTest.getStatus() != null) {
            createTngTest.append(",\nstatus:").append("testStatusJS").append("");
        }

        if(tanaguruTest.getContrast() != null){
            createTngTest.append(",\ncontrast:\"").append(tanaguruTest.getContrast()).append("\"");
        }

        if (tanaguruTest.getFilter() != null) {
            createTngTest.append(",\nfilter:").append(tanaguruTest.getFilter());
        }

        if (tanaguruTest.getAnalyzeElements() != null) {
            createTngTest.append(",\nanalyzeElements:").append(tanaguruTest.getAnalyzeElements());
        }
        createTngTest.append("});");
        
        return createTngTest;
    }
}
