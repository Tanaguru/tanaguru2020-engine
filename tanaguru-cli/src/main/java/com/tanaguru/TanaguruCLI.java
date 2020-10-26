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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
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
                String pattern = "dd-MM-yyyy HH:mm";
                DateFormat df = new SimpleDateFormat(pattern);
                Date today = Calendar.getInstance().getTime();
                String auditName = commandLine.getOptionValue(AUDIT_NAME_OPTION_NAME) != null ? commandLine.getOptionValue(AUDIT_NAME_OPTION_NAME) : "cli "+df.format(today);
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
            LOGGER.error("Unable to parse file or scenario, please check syntax");
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

        Option scenarioOption = new Option(SCENARIO_OPTION_NAME, SCENARIO_OPTION_NAME, false, "Selenese scenario path (if no scenario path informed : mode select scenario in project)");
        scenarioOption.setOptionalArg(true);
        scenarioOption.setArgs(1);
        auditOptionGroup.addOption(scenarioOption);

        Option fileOption = new Option(FILE_OPTION_NAME, FILE_OPTION_NAME, false, "File path (if no file path informed : mode select file in project)");
        fileOption.setOptionalArg(true);
        fileOption.setArgs(1);
        auditOptionGroup.addOption(fileOption);
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
        options.addOption(CRAWLER_MAX_DOCUMENT_OPTION_NAME,CRAWLER_MAX_DOCUMENT_OPTION_NAME,true, "Crawler max document");
        options.addOption(CRAWLER_EXCLUSION_REGEX_OPTION_NAME,CRAWLER_EXCLUSION_REGEX_OPTION_NAME,true, "Crawler exclusion regex");
        options.addOption(CRAWLER_INCLUSION_REGEX_OPTION_NAME,CRAWLER_INCLUSION_REGEX_OPTION_NAME,true, "Crawler inclusion regex");

        options.addOption(WEBDRIVER_RESOLUTIONS_OPTION_NAME,WEBDRIVER_RESOLUTIONS_OPTION_NAME,true, "Webdriver resolutions");
        options.addOption(WEBDRIVER_BROWSER_OPTION_NAME,WEBDRIVER_BROWSER_OPTION_NAME,true, "Webdriver browser");

        return options;
    }

    /**
     * Return an audit configured for PAGE audit or SITE audit depending of parameter type
     * @param commandLine the commandLine of the user with args
     * @param auditName the name of the audit to create
     * @param privateAudit false if the audit is public
     * @param auditType the type of the audit
     * @return audit
     */
    private Optional<Audit> cliPageOrSite(CommandLine commandLine,String auditName, boolean privateAudit, EAuditType auditType) {
        Optional<Audit> audit = Optional.empty();
        HashMap<EAuditParameter, String> auditParameters = fillAuditParameters(commandLine,auditType);
        Optional<Project> project = Optional.empty();
        if(projectRepository.count() >0) {
            showProjects();
            String projectId = askStringId("project");
            if(!projectId.equals("")) {
                try {
                    project = projectRepository.findById(Long.parseLong(projectId));
                }catch(NumberFormatException e) {
                    LOGGER.info("Not a number id writed, no project reference used");
                }
            }else {
                LOGGER.info("No project reference used");
            }
        }else {
            LOGGER.info("No project found, no project reference used");
        }
        long hierarchyId = selectTestHierarchyId();
        TestHierarchy main = testHierarchyRepository.getOne(hierarchyId);
        ArrayList<TestHierarchy> testsHierarchy = new ArrayList<>();
        testsHierarchy.add(main);
        if(project.isEmpty()) {
            audit = Optional.ofNullable(auditFactory.createAudit(auditName, auditParameters, auditType, privateAudit, null, testsHierarchy,main));
        }else {
            audit = Optional.ofNullable(auditFactory.createAudit(auditName, auditParameters, auditType, privateAudit, project.get(), testsHierarchy,main));
        }
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
        if(commandLine.getOptionValue(FILE_OPTION_NAME) != null) {
            audit = cliFileWithPath(commandLine,auditParameters,auditName,privateAudit,auditType);
        }else {
            audit = cliFileWithoutPath(commandLine,auditParameters,auditName,privateAudit,auditType);   //The user want to launch an audit on a resource associated with a project
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
    private Optional<Audit> cliScenario(CommandLine commandLine,String auditName, boolean privateAudit, EAuditType auditType) throws IOException {
        Optional<Audit> audit = Optional.empty();
        HashMap<EAuditParameter, String> auditParameters = fillAuditParameters(commandLine,auditType);
        if(commandLine.getOptionValue(SCENARIO_OPTION_NAME) != null) {
            audit = cliScenarioWithPath(commandLine,auditParameters,auditName,privateAudit,auditType);
        }else {
            audit = cliScenarioWithoutPath(commandLine,auditParameters,auditName,privateAudit,auditType);
        }
        return audit;
    }


    /**
     * Return an audit configured for FILE audit with a new resource file passed in args of the CLI
     * @param commandLine the commandLine of the user with args
     * @param auditParameters the audit parameters
     * @param auditName the name of the audit to create
     * @param privateAudit false if the audit is public
     * @param auditType the type of the audit
     * @return audit
     * @throws IOException
     */
    private Optional<Audit> cliFileWithPath(CommandLine commandLine,HashMap<EAuditParameter, String> auditParameters, String auditName, boolean privateAudit, EAuditType auditType) throws IOException{
        Optional<Audit> audit = Optional.empty();
        String path = commandLine.getOptionValue(FILE_OPTION_NAME);
        File file = new File(path);
        if (file.exists()) {
            String fileContent = FileHelper.getFileContent(file);
            Optional<Project> project = Optional.empty();
            Resource resource = new Resource();
            if(projectRepository.count() >0) {
                showProjects();
                project = selectProject();	    
                resource.setName(askName("resource"));
                resource.setContent(fileContent);
                resource.setProject(project.get());
                Collection<Resource> resources = project.get().getResources();
                resources.add(resource);
                project.get().setResources(resources);
                projectRepository.save(project.get());
                resourceRepository.save(resource);
                auditParameters.put(EAuditParameter.DOM_ID, String.valueOf(resource.getId()));        
                long hierarchyId = selectTestHierarchyId();
                TestHierarchy main = testHierarchyRepository.getOne(hierarchyId);
                ArrayList<TestHierarchy> testsHierarchy = new ArrayList<>();
                testsHierarchy.add(main);
                audit = Optional.ofNullable(auditFactory.createAudit(auditName, auditParameters, auditType, privateAudit, project.get(), testsHierarchy,main));
            }else {
                LOGGER.info("No project found");
            }
        } else {
            LOGGER.error("File : {} could not be find", path);
        }
        return audit;
    }

    /**
     * Return an audit configured for FILE audit with an existing resource file choosed in a project
     * @param commandLine the commandLine of the user with args
     * @param auditParameters the audit parameters
     * @param auditName the name of the audit to create
     * @param privateAudit false if the audit is public
     * @param auditType the type of the audit
     * @return audit
     * @throws IOException
     */
    private Optional<Audit> cliFileWithoutPath(CommandLine commandLine,HashMap<EAuditParameter, String> auditParameters, String auditName, boolean privateAudit, EAuditType auditType) throws IOException{
        Optional<Audit> audit = Optional.empty();
        Optional<Project> project = Optional.empty();
        Optional<Resource> resource = Optional.empty();
        if(resourceRepository.findByIsDeletedIsFalse().size() > 0) {
            showProjectsWithFileResources();
            project = selectProject();
            showFilesResource(project.get().getId());
            long resourceId = askLongId("resource");
            resource = resourceRepository.findById(resourceId);
            while(resource.isEmpty()) {
                resourceId = askLongId("resource");
                resource = resourceRepository.findById(resourceId);  
            }
            auditParameters.put(EAuditParameter.DOM_ID, String.valueOf(resource.get().getId())); 
            long hierarchyId = selectTestHierarchyId();
            TestHierarchy main = testHierarchyRepository.getOne(hierarchyId);
            ArrayList<TestHierarchy> testsHierarchy = new ArrayList<>();
            testsHierarchy.add(main);
            audit = Optional.ofNullable(auditFactory.createAudit(auditName, auditParameters, auditType, privateAudit, project.get(), testsHierarchy,main));
        }else {
            LOGGER.info("No files resources found");
        }
        return audit;
    }

    /**
     * Return an audit configured for SCENARIO audit with a new resource scenario passed in args of the CLI
     * @param commandLine the commandLine of the user with args
     * @param auditParameters the audit parameters
     * @param auditName the name of the audit to create
     * @param privateAudit false if the audit is public
     * @param auditType the type of the audit
     * @return audit
     * @throws IOException
     */
    private Optional<Audit> cliScenarioWithPath(CommandLine commandLine,HashMap<EAuditParameter, String> auditParameters,String auditName, boolean privateAudit,  EAuditType auditType) throws IOException {
        Optional<Audit> audit = Optional.empty();
        String path = commandLine.getOptionValue(SCENARIO_OPTION_NAME);
        File scenarioFile = new File(path);
        if (scenarioFile.exists()) {
            String scenarioContent = Base64.getEncoder().encodeToString(FileHelper.getFileContent(scenarioFile).getBytes());
            Optional<Project> project = Optional.empty();
            Scenario scenario = new Scenario();
            if(projectRepository.count() >0) {
                showProjects();
                project = selectProject();
                scenario.setName(askName("scenario"));
                scenario.setContent(scenarioContent);
                scenario.setProject(project.get());
                Collection<Scenario> scenarios = project.get().getScenarios();
                scenarios.add(scenario);
                project.get().setScenarios(scenarios);
                projectRepository.save(project.get());
                scenarioRepository.save(scenario);
                auditParameters.put(EAuditParameter.SCENARIO_ID, String.valueOf(scenario.getId()));        
                long hierarchyId = selectTestHierarchyId();
                TestHierarchy main = testHierarchyRepository.getOne(hierarchyId);
                ArrayList<TestHierarchy> testsHierarchy = new ArrayList<>();
                testsHierarchy.add(main);
                audit = Optional.ofNullable(auditFactory.createAudit(auditName, auditParameters, auditType, privateAudit, project.get(), testsHierarchy,main));
            }else {
                LOGGER.info("No project found");
            }
        } else {
            LOGGER.error("File : {} could not be find", path);
        }
        return audit;
    }

    /**
     * Return an audit configured for SCENARIO audit with an existing resource scenario choosed in a project
     * @param commandLine the commandLine of the user with args
     * @param auditParameters the audit parameters
     * @param auditName the name of the audit to create
     * @param privateAudit false if the audit is public
     * @param auditType the type of the audit
     * @return audit
     * @throws IOException
     */
    private Optional<Audit> cliScenarioWithoutPath(CommandLine commandLine,HashMap<EAuditParameter, String> auditParameters, String auditName, boolean privateAudit,EAuditType auditType) throws IOException{
        Optional<Audit> audit = Optional.empty();
        Optional<Project> project = Optional.empty();
        Optional<Scenario> scenario = Optional.empty();
        if(scenarioRepository.findByIsDeletedIsFalse().size() > 0) {
            showProjectsWithScenarios();
            project = selectProject();
            showScenarios(project.get().getId());
            long scenarioId = askLongId("scenario");
            scenario = scenarioRepository.findById(scenarioId);
            while(scenario.isEmpty()) {
                System.out.println("Please enter a correct resource's id.");
                scenarioId = askLongId("resource");
                scenario = scenarioRepository.findById(scenarioId);   
            }
            auditParameters.put(EAuditParameter.SCENARIO_ID, String.valueOf(scenario.get().getId())); 
            long hierarchyId = selectTestHierarchyId();
            TestHierarchy main = testHierarchyRepository.getOne(hierarchyId);
            ArrayList<TestHierarchy> testsHierarchy = new ArrayList<>();
            testsHierarchy.add(main);
            audit = Optional.ofNullable(auditFactory.createAudit(auditName, auditParameters, auditType, privateAudit, project.get(), testsHierarchy,main));
        }else {
            LOGGER.info("No files resources found");
        }
        return audit;
    }



    /**
     * Return an input with id from the user, empty for no project reference
     * @param detail the description of the "object" we want id
     * @return the response of the user
     */
    private String askStringId(String detail) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String result = "";
        System.out.print("Please enter the "+detail+" id (or nothing if you want no project reference) : ");
        try {
            result = reader.readLine();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Please enter a correct "+detail+"'s id.");
        }
        return result;
    }

    /**
     * Return an input number from the user
     * @param detail the description of the "object" we want id
     * @return the response of the user
     */
    private long askLongId(String detail) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        long result = -1;
        System.out.print("Please enter the "+detail+" id : ");
        try {
            result = Long.parseLong(reader.readLine());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } catch (RuntimeException e) {
            //LOGGER.error("Unable to parse the input value");
            System.out.println("Please enter a correct "+detail+"'s id.");
        }
        return result;
    }

    /**
     * Return an input string from the user, different of empty string
     * @param the the description of the "object" we want name
     * @return the response of the user
     */
    private String askName(String detail) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String result = "";
        while(result == "") {
            System.out.print("Please enter the "+detail+" name : ");
            try {
                result = reader.readLine();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return result;
    }

    /**
     * Show all the project (id, name, domain)
     */
    private void showProjects() {
        StringBuilder builder = new StringBuilder("\nAvailable projects :");
        for(Project proj : projectRepository.findAll()) {
            builder.append("\nProject ID : ");
            builder.append(proj.getId());
            builder.append(" | Project name : ");
            builder.append(proj.getName());
            builder.append(" | Domain : ");
            builder.append(proj.getDomain());
        }
        System.out.println(builder.toString());
    }

    /**
     * Show all the project (id, name, domain) containing files
     */
    private void showProjectsWithFileResources() {
        StringBuilder builder = new StringBuilder("\nAvailable projects :");
        ArrayList<Project> projects = new ArrayList<Project>();
        for(Resource resource : resourceRepository.findByIsDeletedIsFalse()) {
            Project project = resource.getProject();
            if(!projects.contains(project)) {
                builder.append("\nProject ID : ");
                builder.append(project.getId());
                builder.append(" | Project name : ");
                builder.append(project.getName());
                builder.append(" | Domain : ");
                builder.append(project.getDomain());
            }
            projects.add(project);
        }
        System.out.println(builder.toString());
    }

    /**
     * Show all the project (id, name, domain) containing scenarios
     */
    private void showProjectsWithScenarios() {
        StringBuilder builder = new StringBuilder("\nAvailable projects :");
        ArrayList<Project> projects = new ArrayList<Project>();
        for(Scenario scenario : scenarioRepository.findByIsDeletedIsFalse()) {
            Project project = scenario.getProject();
            if(!projects.contains(project)) {
                builder.append("\nProject ID : ");
                builder.append(project.getId());
                builder.append(" | Project name : ");
                builder.append(project.getName());
                builder.append(" | Domain : ");
                builder.append(project.getDomain());
            }
            projects.add(project);
        }
        System.out.println(builder.toString());
    }

    /**
     * Show all the files resources
     */
    private void showFilesResource(Long projectId) {
        StringBuilder builder = new StringBuilder("\nAvailable files :");
        for(Resource resource : resourceRepository.findAllByProject_IdAndIsDeletedIsFalse(projectId)) {
            builder.append("\nResource ID : ");
            builder.append(resource.getId());
            builder.append(" | Resource name : ");
            builder.append(resource.getName());
        }
        System.out.println(builder.toString());
    }

    /**
     * Show all the scenarios 
     */
    private void showScenarios(Long projectId) {
        StringBuilder builder = new StringBuilder("\nAvailable scenarios :");
        for(Scenario scenario : scenarioRepository.findAllByProject_IdAndIsDeletedIsFalse(projectId)) {
            builder.append("\nScenario ID : ");
            builder.append(scenario.getId());
            builder.append(" | Scenario name : ");
            builder.append(scenario.getName());
        }
        System.out.println(builder.toString());
    }

    /**
     * Return a project selected by the user with its id
     * @return
     */
    private Optional<Project> selectProject(){
        Optional<Project> project = Optional.empty();
        long projectId = askLongId("project");
        project = projectRepository.findById(projectId); 
        while(project.isEmpty()) {
            projectId = askLongId("project");
            project = projectRepository.findById(projectId);
        }
        return project;
    }


    /**
     * Return the id of the hierarchy selected by the user
     * @return
     */
    private long selectTestHierarchyId() {
        long hierarchyId = 1;
        ArrayList<Long> allIdHierarchy = new ArrayList<>();
        StringBuilder builder = new StringBuilder("\nPlease select a repository : ");
        Collection<TestHierarchy> testsHierarchy = testHierarchyRepository.findAllByParentIsNullAndIsDeletedIsFalse();
        for(TestHierarchy testH : testsHierarchy) {
            builder.append("\nID : ");
            builder.append(testH.getId());
            builder.append(" | Name : ");
            builder.append(testH.getName());
            allIdHierarchy.add(testH.getId());
        }
        System.out.println(builder.toString());
        hierarchyId = askLongId("repository");
        while(!allIdHierarchy.contains(hierarchyId)) {
            hierarchyId = askLongId("repository");
        }
        return hierarchyId;
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
                StringBuilder builder = new StringBuilder();
                for(String url : urlsArgs) {
                    builder.append(url);
                    builder.append(";");
                }
                auditParameters.put(EAuditParameter.PAGE_URLS, builder.toString());
            }		 		
        case SITE:
            String[] seedsArgs = commandLine.getOptionValues(SITE_OPTION_NAME);
            if(seedsArgs != null){
                StringBuilder builder = new StringBuilder();
                for(String url : seedsArgs) {
                    builder.append(url);
                    builder.append(";");
                }
                auditParameters.put(EAuditParameter.SITE_SEEDS, builder.toString());
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
