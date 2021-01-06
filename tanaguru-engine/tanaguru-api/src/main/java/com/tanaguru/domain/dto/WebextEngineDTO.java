package com.tanaguru.domain.dto;

public class WebextEngineDTO {

    private long id;
    
    private String engineVersion;
    
    private byte[] engineContent;

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
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
}
