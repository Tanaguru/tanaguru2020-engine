package com.tanaguru;

import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Resource;
import com.tanaguru.domain.entity.audit.Scenario;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.factory.AuditFactory;
import com.tanaguru.helper.FileHelper;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.ProjectRepository;
import com.tanaguru.repository.ResourceRepository;
import com.tanaguru.repository.ScenarioRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.service.AuditRunnerService;
import org.apache.commons.cli.*;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

@SpringBootApplication(scanBasePackages = "com.tanaguru")
public class TanaguruCliDocker implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TanaguruCliDocker.class);

    private final AuditRunnerService auditRunnerService;
    private final AuditRepository auditRepository;

    private static final String TANAGURU_HELP_CMD_SYNTAX = "Tanaguru help";
    private static final String AUDIT_ID = "auditId";

    @Autowired
    public TanaguruCliDocker(AuditRunnerService auditRunnerService,
            AuditRepository auditRepository) {
        this.auditRunnerService = auditRunnerService;
        this.auditRepository = auditRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(TanaguruCliDocker.class, args);
    }

    public void run(String... args) {
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();
        HelpFormatter formatter = new HelpFormatter();
        Audit audit = null;
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("h")) {
                formatter.printHelp(TANAGURU_HELP_CMD_SYNTAX, options);
            } else {
                if (commandLine.hasOption(AUDIT_ID)) {
                    long auditId = Long.valueOf(commandLine.getOptionValue(AUDIT_ID));
                    try {
                        audit = auditRepository.getOne(auditId);
                    }catch(EntityNotFoundException e) {
                        LOGGER.info("No audit found for the ID :"+auditId);
                    }
                } else {
                    throw new IllegalStateException("No audit ID given");
                }
                auditRunnerService.runAudit(audit);
            }

        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            formatter.printHelp(TANAGURU_HELP_CMD_SYNTAX, options);
        } catch (IllegalStateException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private Options getOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Shows this Help");
        
        Option option = new Option(AUDIT_ID,AUDIT_ID,true, "Audit id");
        option.setArgs(1);
        option.setRequired(true);
        options.addOption(option);
        
        return options;
    }
}