package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.TestHierarchy;

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
}
