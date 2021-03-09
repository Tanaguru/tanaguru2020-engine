package com.tanaguru;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.parameter.AuditAuditParameterValue;
import com.tanaguru.repository.AuditAuditParameterValueRepository;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.service.AuditRunnerService;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.tanaguru")
public class TanaguruCliDocker implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TanaguruCliDocker.class);

    private final AuditRunnerService auditRunnerService;
    private final AuditRepository auditRepository;
    private final AuditAuditParameterValueRepository auditAuditParameterValueRepository;

    private static final String TANAGURU_HELP_CMD_SYNTAX = "Tanaguru help";
    private static final String AUDIT_ID = "auditId";

    @Autowired
    public TanaguruCliDocker(AuditRunnerService auditRunnerService,
            AuditRepository auditRepository,
            AuditAuditParameterValueRepository auditAuditParameterValueRepository) {
        this.auditRunnerService = auditRunnerService;
        this.auditRepository = auditRepository;
        this.auditAuditParameterValueRepository = auditAuditParameterValueRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(TanaguruCliDocker.class, args);
    }

    public void run(String... args) {
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();
        HelpFormatter formatter = new HelpFormatter();
        Optional<Audit> audit = Optional.empty();
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("h")) {
                formatter.printHelp(TANAGURU_HELP_CMD_SYNTAX, options);
            } else if (commandLine.hasOption(AUDIT_ID)) {
                Long auditId = Long.valueOf(commandLine.getOptionValue(AUDIT_ID));
                audit = auditRepository.findById(auditId);
                if(audit.isPresent()) {
                    //in order to avoid the lazy initialization we recover the parameters manually and then set them to the audit
                    Collection <AuditAuditParameterValue> val = auditAuditParameterValueRepository.findAllByAudit(audit.get());
                    audit.get().setParameters(val);
                    auditRunnerService.runAudit(audit.get());
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