package com.tanaguru.service.impl;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tanaguru.domain.entity.audit.WebextEngine;
import com.tanaguru.repository.WebextEngineRepository;
import com.tanaguru.service.WebextEngineService;

@Service
@Transactional
public class WebextEngineServiceImpl implements WebextEngineService {
    private final Logger LOGGER = LoggerFactory.getLogger(WebextEngineServiceImpl.class);

    private final String coreScript;
    private final String coreScriptVersion;
    
    private final WebextEngineRepository webextEngineRepository;
    
    @Autowired
    public WebextEngineServiceImpl(
            String coreScript,
            String coreScriptVersion,
            WebextEngineRepository webextEngineRepository) {
        this.coreScript = coreScript;
        this.coreScriptVersion = coreScriptVersion;
        this.webextEngineRepository = webextEngineRepository;
    }
    

    @PostConstruct
    @Transactional
    private void insertBaseWebextEngine() throws IOException {
        try {
            if(!webextEngineRepository.findByEngineVersion(Integer.valueOf(coreScriptVersion)).isPresent()) {
                WebextEngine webextEngine = new WebextEngine();
                webextEngine.setEngineVersion(Integer.valueOf(coreScriptVersion));
                webextEngine.setEngineContent(coreScript.getBytes());
                webextEngine.setEngineName("webext_engine");
                webextEngineRepository.save(webextEngine);
            }
        }catch(NumberFormatException e) {
            LOGGER.error("Unable to parse string to integer :", e);
        }
    }
}
