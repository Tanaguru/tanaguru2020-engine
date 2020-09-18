package com.tanaguru.service;

public interface ScenarioService {
    /**
     *
     * @param scenario The  @see Scenario to check validity of
     * @return True if the @see Scenario is valid
     */
    boolean checkScenarioIsValid(String scenario);
}
