package com.tanaguru;

import com.tanaguru.domain.constant.EAuditParameter;
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
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    private static final String WAIT_TIME = "waitTime";
    private static final String BASICAUTH_URL = "basicAuthUrl";
    private static final String BASICAUTH_LOGIN = "basicAuthLogin";
    private static final String BASICAUTH_PASSWORD = "basicAuthPassword";
    
    private static final String ENABLE_SCREENSHOT = "enableScreenshot";

    private static final String CRAWLER_MAX_DEPTH = "crawlerMaxDepth";
    private static final String CRAWLER_MAX_DURATION = "crawlerMaxDuration";
    private static final String CRAWLER_MAX_DOCUMENT = "crawlerMaxDocument";
    private static final String CRAWLER_EXCLUSION_REGEX = "crawlerExclusionRegex";
    private static final String CRAWLER_INCLUSION_REGEX = "crawlerInclusionRegex";

    private static final String WEBDRIVER_RESOLUTIONS = "webdriverResolution";
    private static final String WEBDRIVER_BROWSER = "webdriverBrowser";

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
                auditRunnerService.runAudit(audit);
            }
            
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
        siteOption.setValueSeparator(';');
        auditOptionGroup.addOption(siteOption);

        Option pageOption = new Option(PAGE_OPTION_NAME, PAGE_OPTION_NAME, true, "Pages urls");
        pageOption.setArgs(Option.UNLIMITED_VALUES);
        pageOption.setValueSeparator(';');
        auditOptionGroup.addOption(pageOption);

        auditOptionGroup.addOption(new Option(SCENARIO_OPTION_NAME, SCENARIO_OPTION_NAME, true, "Selenese scenario path"));
        auditOptionGroup.addOption(new Option(FILE_OPTION_NAME, FILE_OPTION_NAME, true, "File path"));
        auditOptionGroup.setRequired(true);
        options.addOptionGroup(auditOptionGroup);
        
        options.addOption(WAIT_TIME,WAIT_TIME,true,"Wait time (for JS frameworks like Angular, React...) ");
        options.addOption(BASICAUTH_URL,BASICAUTH_URL,true,"Basic auth url");
        options.addOption(BASICAUTH_LOGIN,BASICAUTH_LOGIN,true, "Basic auth login");
        options.addOption(BASICAUTH_PASSWORD,BASICAUTH_PASSWORD, true, "Basic auth password");
        options.addOption(ENABLE_SCREENSHOT, "Enable screenshots for the audit");
        options.addOption(CRAWLER_MAX_DEPTH,CRAWLER_MAX_DEPTH,true, "Crawler max depth");
        options.addOption(CRAWLER_MAX_DURATION,CRAWLER_MAX_DURATION,true, "Crawler max duration");
        options.addOption(CRAWLER_MAX_DOCUMENT,CRAWLER_MAX_DOCUMENT,true, "Crawler maw document");
        options.addOption(CRAWLER_EXCLUSION_REGEX,CRAWLER_EXCLUSION_REGEX,true, "Crawler exclusion regex");
        options.addOption(CRAWLER_INCLUSION_REGEX,CRAWLER_INCLUSION_REGEX,true, "Crawler inclusion regex");
        options.addOption(WEBDRIVER_RESOLUTIONS,WEBDRIVER_RESOLUTIONS,true, "Webdriver resolutions");
        options.addOption(WEBDRIVER_BROWSER,WEBDRIVER_BROWSER,true, "Webdriver browser");
        
        return options;
    }

    private Optional<Audit> cliSite(CommandLine commandLine) {
    	HashMap<EAuditParameter, String> auditParameters = fillAuditParameters(commandLine,EAuditType.SITE);
        Optional<Audit> audit = Optional.ofNullable(auditFactory.createAudit("", auditParameters, EAuditType.SITE, false, null, new ArrayList<>(),null));
        return audit;
    }

    private Optional<Audit> cliPage(CommandLine commandLine) {
    	HashMap<EAuditParameter, String> auditParameters = fillAuditParameters(commandLine,EAuditType.PAGE);
        Optional<Audit> audit = Optional.ofNullable(auditFactory.createAudit("", auditParameters, EAuditType.PAGE, false, null, new ArrayList<>(),null));
        return audit;
       //return auditRequestFactory.createAuditPageRequest(audit, List.of(urlsArgs));
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
    
    
    /**
     * Filled the hashmap of audit parameters with the correct args from the user command line
     * @param commandLine commandLine wich contains the args
     * @param auditType audit type selected
     * @return hashmap<EAuditParameter,String> filled with the args corresponding
     */
    private HashMap<EAuditParameter, String> fillAuditParameters(CommandLine commandLine, EAuditType auditType){
    	HashMap<EAuditParameter, String> auditParameters = new HashMap<EAuditParameter, String>();
    	switch(auditType) {
    	case PAGE:
    		String[] urlsArgs = commandLine.getOptionValues(PAGE_OPTION_NAME);
    		if(urlsArgs != null){
	    		String urls = "";
	        	for(String url : urlsArgs) {
	        		urls = url + ";";
	        	}
	        	auditParameters.put(EAuditParameter.PAGE_URLS, urls);
    		}		 		
    	case SITE:
    		String[] seedsArgs = commandLine.getOptionValues(SITE_OPTION_NAME);
    		if(seedsArgs != null){
	    		String urls = "";
	        	for(String url : seedsArgs) {
	        		urls = url + ";";
	        	}
	        	auditParameters.put(EAuditParameter.SITE_SEEDS, urls);
    		}
    		String crawlerMaxDepth = commandLine.getOptionValue(CRAWLER_MAX_DEPTH);
    		if(crawlerMaxDepth != null) {
    			auditParameters.put(EAuditParameter.CRAWLER_MAX_DEPTH, crawlerMaxDepth);
    		}
    		String crawlerMaxDuration = commandLine.getOptionValue(CRAWLER_MAX_DURATION);
    		if(crawlerMaxDuration != null) {
    			auditParameters.put(EAuditParameter.CRAWLER_MAX_DURATION, crawlerMaxDuration);
    		}
    		String crawlerMaxDocument = commandLine.getOptionValue(CRAWLER_MAX_DOCUMENT);
    		if(crawlerMaxDocument != null) {
    			auditParameters.put(EAuditParameter.CRAWLER_MAX_DOCUMENT, crawlerMaxDocument);
    		}
    		String crawlerExclusionRegex = commandLine.getOptionValue(CRAWLER_EXCLUSION_REGEX);
    		if(crawlerExclusionRegex != null) {
    			auditParameters.put(EAuditParameter.CRAWLER_EXCLUSION_REGEX, crawlerExclusionRegex);
    		}
    		String crawlerInclusionRegex = commandLine.getOptionValue(CRAWLER_INCLUSION_REGEX);
    		if(crawlerInclusionRegex != null) {
    			auditParameters.put(EAuditParameter.CRAWLER_INCLUSION_REGEX, crawlerInclusionRegex);
    		}
    	case SCENARIO:
    		
    	case UPLOAD:
    		
    		
    	}
    	String waitTime = commandLine.getOptionValue(WAIT_TIME);
		if(waitTime != null) {
			auditParameters.put(EAuditParameter.WAIT_TIME, waitTime);
		}
		String login = commandLine.getOptionValue(BASICAUTH_LOGIN);
		if(login != null) {
			auditParameters.put(EAuditParameter.BASICAUTH_LOGIN, login);
		}
		//password : a voir niveau sécurité, mieux comprendre ici si on doit faire ça
		String password = commandLine.getOptionValue(BASICAUTH_PASSWORD);
		if(login != null) {
			auditParameters.put(EAuditParameter.BASICAUTH_PASSWORD, password);
		}
		String basicAuthUrl = commandLine.getOptionValue(BASICAUTH_URL);
		if(basicAuthUrl != null) {
			auditParameters.put(EAuditParameter.BASICAUTH_URL, basicAuthUrl);
		}
		String webdriverResolutions = commandLine.getOptionValue(WEBDRIVER_RESOLUTIONS);
		if(webdriverResolutions != null) {
			auditParameters.put(EAuditParameter.WEBDRIVER_RESOLUTIONS, webdriverResolutions);
		}
		String webdriverBrowser = commandLine.getOptionValue(WEBDRIVER_BROWSER);
		if(webdriverBrowser != null) {
			auditParameters.put(EAuditParameter.WEBDRIVER_BROWSER, webdriverBrowser);
		}
		if(commandLine.hasOption(ENABLE_SCREENSHOT)) {
			auditParameters.put(EAuditParameter.ENABLE_SCREENSHOT, "True");
		}  	
    	return auditParameters;
    }
}
