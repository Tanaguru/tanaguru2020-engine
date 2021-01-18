package com.tanaguru.service.impl;

import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.service.TanaguruTestService;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

@Service
@Transactional
public class TanaguruTestServiceImpl implements TanaguruTestService {

    public TanaguruTestServiceImpl() {
    }

    /**
     * Return a json object with the information of the tanaguru test
     * @param tanaguruTest
     * @return json object
     */
    @Override
    public JSONObject toJson(TanaguruTest tanaguruTest) {
        JSONObject tanaguruTestJson = new JSONObject();
        tanaguruTestJson.put("name", tanaguruTest.getName());
        tanaguruTestJson.put("description", tanaguruTest.getDescription());
        tanaguruTestJson.put("id", tanaguruTest.getId());
        tanaguruTestJson.put("tags", tanaguruTest.getTags());
        return tanaguruTestJson;
    }
}
