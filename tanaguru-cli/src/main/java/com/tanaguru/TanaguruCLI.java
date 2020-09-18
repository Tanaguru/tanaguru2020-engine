package com.tanaguru;

import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.factory.AuditFactory;
import com.tanaguru.helper.FileHelper;
import com.tanaguru.service.AuditRunnerService;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@SpringBootApplication(scanBasePackages = "com.tanaguru")
public class TanaguruCLI implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TanaguruCLI.class);

    private final AuditFactory auditFactory;
    private final AuditRunnerService auditRunnerService;

    private static final String TANAGURU_HELP_CMD_SYNTAX = "Tanaguru help";
    private static final String PAGE_OPTION_NAME = "pages";
    private static final String SITE_OPTION_NAME = "site";
    private static final String SCENARIO_OPTION_NAME = "scenario";
    private static final String FILE_OPTION_NAME = "file";

    @Autowired
    public TanaguruCLI(AuditFactory auditFactory, AuditRunnerService auditRunnerService) {
        this.auditFactory = auditFactory;
        this.auditRunnerService = auditRunnerService;
    }

    public static void main(String[] args) {
        SpringApplication.run(TanaguruCLI.class, args);
    }

    @Override
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
                if (commandLine.hasOption(SITE_OPTION_NAME)) {
                    audit = cliSite(commandLine).orElseThrow(
                            () -> new IllegalStateException("Unable to create site audit")
                    );
                } else if (commandLine.hasOption(PAGE_OPTION_NAME)) {
                    audit = cliPage(commandLine).orElseThrow(
                            () -> new IllegalStateException("Unable to create page audit")
                    );
                } else if (commandLine.hasOption(SCENARIO_OPTION_NAME)) {
                    audit = cliScenario(commandLine).orElseThrow(
                            () -> new IllegalStateException("Unable to create scenario audit")
                    );
                } else if (commandLine.hasOption(FILE_OPTION_NAME)) {
                    audit = cliFile(commandLine).orElseThrow(
                            () -> new IllegalStateException("Unable to create file audit")
                    );
                } else {
                    throw new IllegalStateException("No audit type given");
                }
            }

            auditRunnerService.runAudit(audit);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            formatter.printHelp(TANAGURU_HELP_CMD_SYNTAX, options);
        } catch (IOException e) {
            LOGGER.error("Unable to parse scenario, please check syntax");
        } catch (IllegalStateException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private Options getOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Shows this Help");
        OptionGroup auditOptionGroup = new OptionGroup();

        Option siteOption = new Option(SITE_OPTION_NAME, SITE_OPTION_NAME, true, "Site seeds");
        siteOption.setArgs(Option.UNLIMITED_VALUES);
        siteOption.setValueSeparator(',');
        auditOptionGroup.addOption(siteOption);

        Option pageOption = new Option(PAGE_OPTION_NAME, PAGE_OPTION_NAME, true, "Pages urls");
        pageOption.setArgs(Option.UNLIMITED_VALUES);
        pageOption.setValueSeparator(',');
        auditOptionGroup.addOption(pageOption);

        auditOptionGroup.addOption(new Option(SCENARIO_OPTION_NAME, SCENARIO_OPTION_NAME, true, "Selenese scenario path"));
        options.addOptionGroup(auditOptionGroup);
        return options;
    }

    private Optional<Audit> cliSite(CommandLine commandLine) {
        //Audit audit = auditFactory.createAudit("", new HashMap<>(), EAuditType.SITE, false, null);
        String[] seedsArgs = commandLine.getOptionValues(SITE_OPTION_NAME);
        return Optional.empty();
    }

    private Optional<Audit> cliPage(CommandLine commandLine) {
        //Audit audit = auditFactory.createAudit("", new HashMap<>(), EAuditType.PAGE, false, null);
        String[] urlsArgs = commandLine.getOptionValues(PAGE_OPTION_NAME);
        return Optional.empty();
       // return auditRequestFactory.createAuditPageRequest(audit, List.of(urlsArgs));
    }

    private Optional<Audit> cliScenario(CommandLine commandLine) throws IOException {
        Optional<Audit> auditSeleneseRequestOptional = Optional.empty();
        String path = commandLine.getOptionValue(SCENARIO_OPTION_NAME);
        File scenarioFile = new File(path);
        if (scenarioFile.exists()) {
            String scenario = FileHelper.getFileContent(scenarioFile);
           // Audit audit = auditFactory.createAudit("", new HashMap<>(), EAuditType.SCENARIO, false, null);
            //auditSeleneseRequestOptional = auditRequestFactory.createAuditSeleneseRequest(audit, scenario);
        } else {
            LOGGER.error("File : {} could not be find", path);
        }
        return auditSeleneseRequestOptional;
    }

    private Optional<Audit> cliFile(CommandLine commandLine) throws IOException {
        Optional<Audit> auditUploadRequestOptional = Optional.empty();
        String path = commandLine.getOptionValue(FILE_OPTION_NAME);
        File scenarioFile = new File(path);
        if (scenarioFile.exists()) {
            String scenario = FileHelper.getFileContent(scenarioFile);
            //Audit audit = auditFactory.createAudit("", new HashMap<>(), EAuditType.UPLOAD, false, null);
            //auditUploadRequestOptional = auditRequestFactory.createAuditUploadRequest(audit, scenario);
        } else {
            LOGGER.error("File : {} could not be find", path);
        }
        return auditUploadRequestOptional;
    }
}
