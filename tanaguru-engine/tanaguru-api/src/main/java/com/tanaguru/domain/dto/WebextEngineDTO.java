package com.tanaguru.domain.dto;

public class WebextEngineDTO {

    private long id;
    
    private String engineName;

    private int engineVersion;
    
    private byte[] engineContent;

    public int getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(int engineVersion) {
        this.engineVersion = engineVersion;
    }

    public byte[] getEngineContent() {
        return engineContent;
    }

    public void setEngineContent(byte[] engineContent) {
        this.engineContent = engineContent;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public String getEngineName() {
        return engineName;
    }
    
    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }
}
