package com.tanaguru.runner;


import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.selenese.command.SeleneseAudit;
import com.tanaguru.selenese.command.SeleneseClick;
import com.tanaguru.selenese.command.SeleneseOpen;
import jp.vmi.selenium.selenese.Parser;
import jp.vmi.selenium.selenese.Runner;
import jp.vmi.selenium.selenese.Selenese;
import jp.vmi.selenium.selenese.TestProject;
import jp.vmi.selenium.selenese.command.CommandFactory;
import jp.vmi.selenium.selenese.command.ICommand;
import jp.vmi.selenium.selenese.result.Result;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class AuditRunnerSelenese extends AbstractAuditRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRunnerSelenese.class);
    private String scenario;

    public AuditRunnerSelenese(
            Collection<TanaguruTest> tanaguruTests,
            Audit audit,
            String scenario,
            RemoteWebDriver driver,
            String coreScript,
            long waitTime,
            Collection<Integer> resolutions,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword,
            boolean enableScreenShot) {
        super(tanaguruTests,
                audit,
                driver,
                coreScript,
                waitTime,
                resolutions,
                basicAuthUrl,
                basicAuthLogin,
                basicAuthPassword,
                enableScreenShot);
        this.scenario = scenario;
    }

    @Override
    protected void runImpl() {
        Runner runner = getRunner();
        try {
            Selenese selenese = getSeleneseScript(runner);
            TestProject project = (TestProject) selenese;
            for (Selenese test : project.getSeleneseList()) {
                if (super.isStop()) {
                    LOGGER.warn("[Audit {}] Interrupting current audit", super.getAudit().getId());
                    break;
                } else {
                    EAuditLogLevel level = EAuditLogLevel.INFO;
                    Result result = runner.execute(test);
                    if(result.getLevel() == Result.Level.ERROR){
                        level = EAuditLogLevel.ERROR;
                    }else if(result.getLevel() == Result.Level.WARNING){
                        level = EAuditLogLevel.WARNING;
                    }

                    auditLog(level, result.getMessage());
                }

            }
        } catch (IOException e) {
            auditLog(EAuditLogLevel.ERROR, "Unable to parse selenese scenario.\n" + e.getMessage());
            LOGGER.error("Unable to parse selenese scenario.\n{}", e.getMessage());
        }
    }

    private Runner getRunner() {
        Runner runner = new Runner();
        CommandFactory cf = runner.getCommandFactory();
        cf.registerCommandFactory((index, name, args) -> {
            ICommand res;
            switch (name) {
                case "click":
                    res = new SeleneseClick(this, index, name, args);
                    break;
                case "store":
                    res = new SeleneseAudit(this, index, name, args);
                    break;
                case "open":
                    res = new SeleneseOpen(this, index, name, args);
                    break;
                default:
                    res = null;
            }
            return res;
        });
        runner.setDriver(getTanaguruDriver());
        return runner;
    }

    private Selenese getSeleneseScript(Runner runner) throws IOException {
        InputStream input = new ByteArrayInputStream(scenario.getBytes());
        Selenese selenese = Parser.parse("selenese.side", input, runner.getCommandFactory());
        input.close();
        return selenese;
    }

}

