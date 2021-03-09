package com.tanaguru;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.service.AuditRunnerService;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("h")) {
                formatter.printHelp(TANAGURU_HELP_CMD_SYNTAX, options);
            } else if (commandLine.hasOption(AUDIT_ID)) {
                long auditId = Long.valueOf(commandLine.getOptionValue(AUDIT_ID));
                try {
                    Audit audit = auditRepository.getOne(auditId);
                    auditRunnerService.runAudit(audit);
                }catch(EntityNotFoundException e) {
                    LOGGER.info("No audit found for the ID :"+auditId);
                }
            }else {
                throw new IllegalStateException("No audit ID given");
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
        
        Option auditIdOption = new Option(AUDIT_ID,AUDIT_ID,true, "Audit id");
        auditIdOption.setArgs(1);
        auditIdOption.setRequired(true);
        options.addOption(auditIdOption);
        
        return options;
    }
}