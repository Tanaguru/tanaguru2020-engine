package com.tanaguru.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditLog;
import com.tanaguru.repository.AuditLogRepository;
import com.tanaguru.service.AuditLogService;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final Logger LOGGER = LoggerFactory.getLogger(AuditSchedulerServiceImpl.class);
    private final AuditLogRepository auditLogRepository;
    
    @Autowired
    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Override
    public JSONObject toJson(Audit audit) {
        JSONObject jsonAuditLogsObject = new JSONObject();
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        Collection<AuditLog> auditLogs =  auditLogRepository.findAllByAudit(audit);
        if(!auditLogs.isEmpty()) {
            for(AuditLog log : auditLogs) {
                try {
                    jsonAuditLogsObject.append("auditLogs", new JSONObject(mapper.writeValueAsString(log)));
                } catch (JSONException | JsonProcessingException e) {
                    LOGGER.error("Error in serializing audit log");
                }   
            }
        }
        return jsonAuditLogsObject;
    }

}
