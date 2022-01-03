package com.tanaguru.runner;

import org.openqa.selenium.remote.RemoteWebDriver;

public class ScriptExecutor implements Runnable {
    
    private volatile String result;
    private String script;
    private final RemoteWebDriver tanaguruDriver;
    
    public ScriptExecutor(String script, RemoteWebDriver tanaguruDriver ) {
        this.script = script;
        this.tanaguruDriver = tanaguruDriver;
    }

    @Override
    public void run() {
       result = (String) tanaguruDriver.executeScript(script.toString());
    }

    public String getResult() {
        return result;
    }
    
}
