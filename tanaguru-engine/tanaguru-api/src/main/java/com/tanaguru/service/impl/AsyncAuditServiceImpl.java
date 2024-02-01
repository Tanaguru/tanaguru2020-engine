package com.tanaguru.service.impl;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.service.AsyncAuditService;
import com.tanaguru.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class AsyncAuditServiceImpl implements AsyncAuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncAuditServiceImpl.class);

    private final AuditService auditService;
    private final AuditRepository auditRepository;
    private final Set<Audit> deletionSet = Collections.synchronizedSet( new HashSet<>());
    @Autowired
    private final Environment environment;

    public AsyncAuditServiceImpl(AuditService auditService, AuditRepository auditRepository, Environment environment) {
        this.auditService = auditService;
        this.auditRepository = auditRepository;
        this.environment = environment;
    }

    @PostConstruct
    private void startDeletedAuditCleanup() {
    	LOGGER.info("Update database cleanup properties.");
    	Integer totalPurge = auditService.getAllAuditIncorrectlyDeleted().size();
    	
    	if(0 == totalPurge || environment.getProperty("purge.status").equals("init")) {
            String status = 0 == totalPurge ? "useless" : "topurge";
            updatePurgeProperties(status, totalPurge);
    	}
    	
    	LOGGER.info("Resume audit deletion");
        auditRepository.findAllByDeletedIsTrue().forEach(this::deleteAudit);
    }
    
    public void updatePurgeProperties(String status, int total) {
    	if(environment instanceof ConfigurableEnvironment) {
    		ConfigurableEnvironment env = (ConfigurableEnvironment) environment;            
            MutablePropertySources mps = env.getPropertySources();
            Map<String, Object> prop = new HashMap<>();
            
            prop.put("purge.total", total);
            prop.put("purge.status", status);
            
        	mps.addFirst(new MapPropertySource("rest", prop));        	
    	}
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public void deleteAudit(Audit audit) {
        if(!deletionSet.contains(audit)){
            deletionSet.add(audit);
            auditService.deleteAudit(audit);
        }
    }
}
