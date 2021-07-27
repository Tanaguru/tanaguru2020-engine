package com.tanaguru.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.service.AuditActService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

@Service
public class AuditActServiceImpl implements AuditActService {

    private final Logger LOGGER = LoggerFactory.getLogger(PageServiceImpl.class);
    private final ActRepository actRepository;

    @Autowired
    public AuditActServiceImpl(ActRepository actRepository) {
        this.actRepository = actRepository;
    }

    @Override
    public JSONObject toJson(Audit audit) {
        JSONObject jsonActObject = new JSONObject();
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        Optional<Act> act = actRepository.findByAudit(audit);
        if (act.isPresent()) {
            try {
                jsonActObject = new JSONObject(mapper.writeValueAsString(act.get()));
            } catch (JSONException | JsonProcessingException e) {
                LOGGER.error("Error in serializing act to json");
            }
        }
        return jsonActObject;
    }
}
