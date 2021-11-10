package com.tanaguru.runner.factory;

import com.google.gson.Gson;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;

@Component
public class ScriptFactoryImpl implements ScriptFactory {
    /**
     * Create an executable script.
     *
     * @param coreScript       the content script
     * @param tanaguruTestList tanaguru tests
     * @return the full script
     */
    public HashMap<String,String> create(String coreScript, Collection<TanaguruTest> tanaguruTestList) {
        HashMap map = new HashMap<>();
        StringBuilder strb = new StringBuilder();
        Gson gson = new Gson();
        strb.append(coreScript);
        strb.append("\n");
        strb.append("getNACat();\n");
        StringBuilder imgScript = new StringBuilder();
        //a ajouter que sur le premier script normalement pour gagner en performances
        //Todo : chercher pour adapter et ne l'ajouter que sur le premier script (window.var?)
        imgScript.append(coreScript);
        StringBuilder cadresScript = new StringBuilder();
        //cadresScript.append(coreScript);
        for (TanaguruTest tanaguruTest : tanaguruTestList) {
            StringBuilder createTngTest = new StringBuilder();
            String code = "0";
            for(TestHierarchy th : tanaguruTest.getTestHierarchies()) {
                if(th.getCode().startsWith("2.")) {
                    code = "2";
                }else if(th.getCode().startsWith("1.")) {
                    code = "1";
                }
            }
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
            if(code == "1") {
                imgScript.append(createTngTest);
            }else if(code == "2") {
                cadresScript.append(createTngTest);
            }
        }
        //que sur le dernier script normalement (dernière catégorie) removeAllDataTNG
        imgScript.append("\nremoveAllDataTNG();");
        imgScript.append("\nreturn JSON.stringify(loadTanaguruTests());");
        cadresScript.append("\nremoveAllDataTNG();");
        cadresScript.append("\nreturn JSON.stringify(loadTanaguruTests());");
        //strb.append("\nremoveAllDataTNG();");
        //strb.append("\nreturn JSON.stringify(loadTanaguruTests());");
        map.put("images", imgScript.toString());
        map.put("cadres", cadresScript.toString());
        return map;
    }
}
