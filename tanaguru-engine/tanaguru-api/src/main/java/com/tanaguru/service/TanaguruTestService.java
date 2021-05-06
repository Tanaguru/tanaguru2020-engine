package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.TanaguruTest;
import org.json.JSONObject;

public interface TanaguruTestService {
    
    /**
     * Return a json with the information of the tanaguru test
     * @param tanaguruTest The given @see TanaguruTest
     * @return json object
     */
    JSONObject toJson(TanaguruTest tanaguruTest);
    
}
