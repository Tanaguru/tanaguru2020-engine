package com.tanaguru.service.impl;

import com.tanaguru.repository.ScenarioRepository;
import com.tanaguru.service.ScenarioService;
import jp.vmi.selenium.selenese.Parser;
import jp.vmi.selenium.selenese.Runner;
import jp.vmi.selenium.selenese.Selenese;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author rcharre
 */
@Service
@Transactional
public class ScenarioServiceImpl implements ScenarioService {
    private final ScenarioRepository scenarioRepository;

    public ScenarioServiceImpl(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    public boolean checkScenarioIsValid(String scenario) {
        Runner runner = new Runner();
        InputStream input = new ByteArrayInputStream(scenario.getBytes());
        try {
            Selenese selenese = Parser.parse("selenese.side", input, runner.getCommandFactory());
            return selenese != null && !selenese.isError();
        } catch (Exception e) {
            return false;
        }
    }
}
