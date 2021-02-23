package com.tanaguru.domain.dto;

public class ErrorDTO {
    private String error;
    private String[] content;

    public ErrorDTO(String error) {
        this.error = error;
        this.content = null;
    }
    
    public ErrorDTO(String error, String[] content) {
        this.error = error;
        this.content = content;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
    public String[] getContent() {
        return content;
    }
    
    public void setContent(String[] content) {
        this.content = content;
    }
}
