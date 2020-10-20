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
import com.tanaguru.repository.ProjectRepository;
import com.tanaguru.repository.ResourceRepository;
import com.tanaguru.repository.ScenarioRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.service.AuditRunnerService;
import org.apache.commons.cli.*;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

@SpringBootApplication(scanBasePackages = "com.tanaguru")
public class TanaguruCLI implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TanaguruCLI.class);

    private final AuditFactory auditFactory;
    private final AuditRunnerService auditRunnerService;
    private ProjectRepository projectRepository;
    private ResourceRepository resourceRepository;
    private ScenarioRepository scenarioRepository;
    private TestHierarchyRepository testHierarchyRepository;

    private static final String TANAGURU_HELP_CMD_SYNTAX = "Tanaguru help";
    private static final String PAGE_OPTION_NAME = "pages";
    private static final String SITE_OPTION_NAME = "site";
    private static final String SCENARIO_OPTION_NAME = "scenario";
    private static final String FILE_OPTION_NAME = "file";
    private static final String AUDIT_NAME_OPTION_NAME = "name";
    private static final String PRIVATE_AUDIT_OPTION_NAME = "private";

    private static final String WAIT_TIME_OPTION_NAME = "waitTime";
    private static final String ENABLE_SCREENSHOT_OPTION_NAME = "enableScreenshot";
    
    private static final String BASICAUTH_URL_OPTION_NAME = "basicAuthUrl";
    private static final String BASICAUTH_LOGIN_OPTION_NAME = "basicAuthLogin";
    private static final String BASICAUTH_PASSWORD_OPTION_NAME = "basicAuthPassword";

    private static final String CRAWLER_MAX_DEPTH_OPTION_NAME = "crawlerMaxDepth";
    private static final String CRAWLER_MAX_DURATION_OPTION_NAME = "crawlerMaxDuration";
    private static final String CRAWLER_MAX_DOCUMENT_OPTION_NAME = "crawlerMaxDocument";
    private static final String CRAWLER_EXCLUSION_REGEX_OPTION_NAME = "crawlerExclusionRegex";
    private static final String CRAWLER_INCLUSION_REGEX_OPTION_NAME = "crawlerInclusionRegex";

    private static final String WEBDRIVER_RESOLUTIONS_OPTION_NAME = "webdriverResolution";
    private static final String WEBDRIVER_BROWSER_OPTION_NAME = "webdriverBrowser";

    @Autowired
    public TanaguruCLI(AuditFactory auditFactory, 
    		AuditRunnerService auditRunnerService, 
    		ProjectRepository projectRepository, 
    		ResourceRepository resourceRepository,
    		ScenarioRepository scenarioRepository,
    		TestHierarchyRepository testHierarchyRepository) {
        this.auditFactory = auditFactory;
        this.auditRunnerService = auditRunnerService;
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
        this.scenarioRepository = scenarioRepository;
        this.testHierarchyRepository = testHierarchyRepository;
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
            	String auditName = commandLine.getOptionValue(AUDIT_NAME_OPTION_NAME) != null ? commandLine.getOptionValue(AUDIT_NAME_OPTION_NAME) : "";
        		boolean privateAudit = commandLine.hasOption(PRIVATE_AUDIT_OPTION_NAME);
                if (commandLine.hasOption(SITE_OPTION_NAME)) {
                    audit = cliPageOrSite(commandLine, auditName, privateAudit, EAuditType.SITE).orElseThrow(
                            () -> new IllegalStateException("Unable to create site audit")
                    );
                } else if (commandLine.hasOption(PAGE_OPTION_NAME)) {
                    audit = cliPageOrSite(commandLine, auditName, privateAudit, EAuditType.PAGE).orElseThrow(
                            () -> new IllegalStateException("Unable to create page audit")
                    );
                } else if (commandLine.hasOption(SCENARIO_OPTION_NAME)) {
                    audit = cliScenario(commandLine, auditName, privateAudit, EAuditType.SCENARIO).orElseThrow(
                            () -> new IllegalStateException("Unable to create scenario audit")
                    );
                } else if (commandLine.hasOption(FILE_OPTION_NAME)) {
                    audit = cliFile(commandLine, auditName, privateAudit, EAuditType.UPLOAD).orElseThrow(
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
        
        options.addOption(AUDIT_NAME_OPTION_NAME,AUDIT_NAME_OPTION_NAME,true, "Audit name");
        options.addOption(PRIVATE_AUDIT_OPTION_NAME, "Private audit");
        options.addOption(WAIT_TIME_OPTION_NAME,WAIT_TIME_OPTION_NAME,true,"Wait time (for JS frameworks like Angular, React...) ");
        
        options.addOption(BASICAUTH_URL_OPTION_NAME,BASICAUTH_URL_OPTION_NAME,true,"Basic auth url");
        options.addOption(BASICAUTH_LOGIN_OPTION_NAME,BASICAUTH_LOGIN_OPTION_NAME,true, "Basic auth login");
        options.addOption(BASICAUTH_PASSWORD_OPTION_NAME,BASICAUTH_PASSWORD_OPTION_NAME, true, "Basic auth password");
        options.addOption(ENABLE_SCREENSHOT_OPTION_NAME, "Enable screenshots for the audit");
        
        options.addOption(CRAWLER_MAX_DEPTH_OPTION_NAME,CRAWLER_MAX_DEPTH_OPTION_NAME,true, "Crawler max depth");
        options.addOption(CRAWLER_MAX_DURATION_OPTION_NAME,CRAWLER_MAX_DURATION_OPTION_NAME,true, "Crawler max duration");
        options.addOption(CRAWLER_MAX_DOCUMENT_OPTION_NAME,CRAWLER_MAX_DOCUMENT_OPTION_NAME,true, "Crawler maw document");
        options.addOption(CRAWLER_EXCLUSION_REGEX_OPTION_NAME,CRAWLER_EXCLUSION_REGEX_OPTION_NAME,true, "Crawler exclusion regex");
        options.addOption(CRAWLER_INCLUSION_REGEX_OPTION_NAME,CRAWLER_INCLUSION_REGEX_OPTION_NAME,true, "Crawler inclusion regex");
        
        options.addOption(WEBDRIVER_RESOLUTIONS_OPTION_NAME,WEBDRIVER_RESOLUTIONS_OPTION_NAME,true, "Webdriver resolutions");
        options.addOption(WEBDRIVER_BROWSER_OPTION_NAME,WEBDRIVER_BROWSER_OPTION_NAME,true, "Webdriver browser");
        
        return options;
    }

    /**
     * Return an audit configured for PAGE audit or SITE audit depending of type of the parameter
     * @param commandLine the commandLine of the user with args
     * @param auditName the name of the audit to create
     * @param privateAudit false if the audit is public
     * @param auditType the type of the audit
     * @return audit
     */
    private Optional<Audit> cliPageOrSite(CommandLine commandLine,String auditName, boolean privateAudit, EAuditType auditType) {
    	HashMap<EAuditParameter, String> auditParameters = fillAuditParameters(commandLine,auditType);
    	Project project = null;
    	if(privateAudit) {
    		showProjects();
    		String projectName = askDetailsToUser("project name");
    		project = projectRepository.findByName(projectName);
    		while(project == null) {
    			System.out.println("Please enter a correct project's name.");
    			projectName = askDetailsToUser("project name");
    			project = projectRepository.findByName(projectName);
    		}
    	}
    	TestHierarchy main = testHierarchyRepository.getOne(Long.valueOf(1)); //by default use the wcag20 (first line table test_hierarchy)
    	ArrayList<TestHierarchy> testsHierarchy = new ArrayList<>();
    	testsHierarchy.add(main);
        Optional<Audit> audit = Optional.ofNullable(auditFactory.createAudit(auditName, auditParameters, auditType, privateAudit, project, testsHierarchy,main));
        return audit;
    }

    /**
     * Return an audit configured for FILE audit 
     * @param commandLine the commandLine of the user with args
     * @param auditName the name of the audit to create
     * @param privateAudit false if the audit is public
     * @param auditType the type of the audit
     * @return audit
     * @throws IOException
     */
    private Optional<Audit> cliFile(CommandLine commandLine,String auditName, boolean privateAudit, EAuditType auditType) throws IOException {
    	Optional<Audit> audit = Optional.empty();
    	HashMap<EAuditParameter, String> auditParameters = fillAuditParameters(commandLine,auditType);
    	String path = commandLine.getOptionValue(FILE_OPTION_NAME);
        File file = new File(path);
        if (file.exists()) {
            String fileContent = FileHelper.getFileContent(file);
            Project project = null;
            Resource resource = new Resource();
        	if(privateAudit) {
        		showProjects();
        		String projectName = askDetailsToUser("project name");
        		project = projectRepository.findByName(projectName);
        		while(project == null) {
        			System.out.println("Please enter a correct project's name.");
        			projectName = askDetailsToUser("project name");
        			project = projectRepository.findByName(projectName);
        		}
        	}else {
        		project = new Project();
        		project.setName(askDetailsToUser("new project name"));
        	}
        	resource.setName(askDetailsToUser("resource file name"));
            resource.setContent(fileContent);
            resource.setProject(project);
            Collection<Resource> resources = new ArrayList<>();
            resources.add(resource);
            project.setResources(resources);
            projectRepository.save(project);
            resourceRepository.save(resource);
            auditParameters.put(EAuditParameter.DOM_ID, String.valueOf(resource.getId()));        
            TestHierarchy main = testHierarchyRepository.getOne(Long.valueOf(1));
        	ArrayList<TestHierarchy> testsHierarchy = new ArrayList<>();
        	testsHierarchy.add(main);
            audit = Optional.ofNullable(auditFactory.createAudit(auditName, auditParameters, auditType, privateAudit, project, testsHierarchy,main));
        } else {
            LOGGER.error("File : {} could not be find", path);
        }
        return audit;
    }
    
    /**
     * Return an audit configured for SCENARIO audit 
     * @param commandLine the commandLine of the user with args
     * @param auditName the name of the audit to create
     * @param privateAudit false if the audit is public
     * @param auditType the type of the audit
     * @return audit
     * @throws IOException
     */
    private Optional<Audit> cliScenario(CommandLine commandLine,String auditName, boolean privateAudit,  EAuditType auditType) throws IOException {
    	Optional<Audit> audit = Optional.empty();
    	HashMap<EAuditParameter, String> auditParameters = fillAuditParameters(commandLine,auditType);
    	String path = commandLine.getOptionValue(SCENARIO_OPTION_NAME);
        File scenarioFile = new File(path);
        if (scenarioFile.exists()) {
            String scenarioContent = Base64.getEncoder().encodeToString(FileHelper.getFileContent(scenarioFile).getBytes());
            Project project = null;
            Scenario scenario = new Scenario();
        	if(privateAudit) {
        		showProjects();
        		String projectName = askDetailsToUser("project name");
        		project = projectRepository.findByName(projectName);
        		while(project == null) {
        			System.out.println("Please enter a correct project's name.");
        			projectName = askDetailsToUser("project name");
        			project = projectRepository.findByName(projectName);
        		}
        	}else {
        		project = new Project();
        		project.setName(askDetailsToUser("new project name"));
        	}
        	scenario.setName(askDetailsToUser("resource scenario name"));
            scenario.setContent(scenarioContent);
            scenario.setProject(project);
            Collection<Scenario> scenarios = new ArrayList<>();
            scenarios.add(scenario);
            project.setScenarios(scenarios);
            projectRepository.save(project);
            scenarioRepository.save(scenario);
            auditParameters.put(EAuditParameter.SCENARIO_ID, String.valueOf(scenario.getId()));        
            TestHierarchy main = testHierarchyRepository.getOne(Long.valueOf(1));
        	ArrayList<TestHierarchy> testsHierarchy = new ArrayList<>();
        	testsHierarchy.add(main);
            audit = Optional.ofNullable(auditFactory.createAudit(auditName, auditParameters, auditType, privateAudit, project, testsHierarchy,main));
        } else {
            LOGGER.error("File : {} could not be find", path);
        }
        return audit;
    }
    
    /**
     * Wait an input from the user in the prompt, different of empty string, for the parameter specified in args
     * @param detailToAsk the parameter to ask
     * @return the response of the user
     */
    private String askDetailsToUser(String detailToAsk) {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    	String result = "";
    	while(result.equals("")) {
    		System.out.print("Please enter "+detailToAsk+" : ");
	    	try {
	    		result = reader.readLine();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
    	}
    	return result;
    }
    
    /**
     * Show all the project with their domain
     */
    private void showProjects() {
    	String projectsAvailable = "Projet disponibles : \n";
		for(Project proj : projectRepository.findAll()) {
			projectsAvailable = "Projet : " +projectsAvailable + proj.getName() + " Domain : "+ proj.getDomain() + "\n";
		}
		System.out.println(projectsAvailable);
    }
    
    /**
     * Filled the hashmap of audit parameters with the args from the user command line
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
	        		urls = urls + url + ";";
	        	}
	        	auditParameters.put(EAuditParameter.PAGE_URLS, urls);
    		}		 		
    	case SITE:
    		String[] seedsArgs = commandLine.getOptionValues(SITE_OPTION_NAME);
    		if(seedsArgs != null){
	    		String urls = "";
	        	for(String url : seedsArgs) {
	        		urls = urls + url + ";";
	        	}
	        	auditParameters.put(EAuditParameter.SITE_SEEDS, urls);
    		}
    		String crawlerMaxDepth = commandLine.getOptionValue(CRAWLER_MAX_DEPTH_OPTION_NAME);
    		if(crawlerMaxDepth != null) {
    			auditParameters.put(EAuditParameter.CRAWLER_MAX_DEPTH, crawlerMaxDepth);
    		}
    		String crawlerMaxDuration = commandLine.getOptionValue(CRAWLER_MAX_DURATION_OPTION_NAME);
    		if(crawlerMaxDuration != null) {
    			auditParameters.put(EAuditParameter.CRAWLER_MAX_DURATION, crawlerMaxDuration);
    		}
    		String crawlerMaxDocument = commandLine.getOptionValue(CRAWLER_MAX_DOCUMENT_OPTION_NAME);
    		if(crawlerMaxDocument != null) {
    			auditParameters.put(EAuditParameter.CRAWLER_MAX_DOCUMENT, crawlerMaxDocument);
    		}
    		String crawlerExclusionRegex = commandLine.getOptionValue(CRAWLER_EXCLUSION_REGEX_OPTION_NAME);
    		if(crawlerExclusionRegex != null) {
    			auditParameters.put(EAuditParameter.CRAWLER_EXCLUSION_REGEX, crawlerExclusionRegex);
    		}
    		String crawlerInclusionRegex = commandLine.getOptionValue(CRAWLER_INCLUSION_REGEX_OPTION_NAME);
    		if(crawlerInclusionRegex != null) {
    			auditParameters.put(EAuditParameter.CRAWLER_INCLUSION_REGEX, crawlerInclusionRegex);
    		}	
    	}
    	String waitTime = commandLine.getOptionValue(WAIT_TIME_OPTION_NAME);
		if(waitTime != null) {
			auditParameters.put(EAuditParameter.WAIT_TIME, waitTime);
		}
		String login = commandLine.getOptionValue(BASICAUTH_LOGIN_OPTION_NAME);
		if(login != null) {
			auditParameters.put(EAuditParameter.BASICAUTH_LOGIN, login);
		}
		//password : a voir niveau sécurité, mieux comprendre ici si on doit faire ça
		String password = commandLine.getOptionValue(BASICAUTH_PASSWORD_OPTION_NAME);
		if(password != null) {
			auditParameters.put(EAuditParameter.BASICAUTH_PASSWORD, password);
		}
		String basicAuthUrl = commandLine.getOptionValue(BASICAUTH_URL_OPTION_NAME);
		if(basicAuthUrl != null) {
			auditParameters.put(EAuditParameter.BASICAUTH_URL, basicAuthUrl);
		}
		String webdriverResolutions = commandLine.getOptionValue(WEBDRIVER_RESOLUTIONS_OPTION_NAME);
		if(webdriverResolutions != null) {
			auditParameters.put(EAuditParameter.WEBDRIVER_RESOLUTIONS, webdriverResolutions);
		}
		String webdriverBrowser = commandLine.getOptionValue(WEBDRIVER_BROWSER_OPTION_NAME);
		if(webdriverBrowser != null) {
			auditParameters.put(EAuditParameter.WEBDRIVER_BROWSER, webdriverBrowser);
		}
		if(commandLine.hasOption(ENABLE_SCREENSHOT_OPTION_NAME)) {
			auditParameters.put(EAuditParameter.ENABLE_SCREENSHOT, "True");
		}  	
    	return auditParameters;
    }
}
