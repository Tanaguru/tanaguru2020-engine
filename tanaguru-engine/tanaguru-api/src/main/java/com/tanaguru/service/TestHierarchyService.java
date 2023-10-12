package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.TestHierarchy;
import org.json.JSONObject;

public interface TestHierarchyService {

    /**
     * Tag to delte recursively a test hierarchy
     * @param testHierarchy The @see TestHierarchy to delete
     */
    void tagDeletedWithChild(TestHierarchy testHierarchy);

    /**
     * Check if reference is used, if true tag to delete else delete the reference
     * @param reference The reference to delete
     */
    void deleteReference(TestHierarchy reference);
    
    /**
     * Set isDefault to true for a given reference
     * If existing previous default reference, set isDefault to false for it
     *
     * @param reference
     */
    void changeDefaultReference(TestHierarchy reference);
    
    /**
     * Return a json object with the information of the test hierarchy
     * @param testHierarchy The given @see TestHierarchy
     * @return json object
     */
    JSONObject toJson(TestHierarchy testHierarchy);
}
