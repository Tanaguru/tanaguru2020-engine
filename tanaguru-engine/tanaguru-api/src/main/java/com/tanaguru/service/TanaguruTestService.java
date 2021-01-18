package com.tanaguru.service;

import org.json.JSONObject;

import com.tanaguru.domain.entity.audit.TanaguruTest;

public interface TanaguruTestService {
    
    /**
     * Return a json with the information of the tanaguru test
     * @param tanaguruTest The given @see TanaguruTest
     * @return json object
     */
    JSONObject toJson(TanaguruTest tanaguruTest);
    
}
