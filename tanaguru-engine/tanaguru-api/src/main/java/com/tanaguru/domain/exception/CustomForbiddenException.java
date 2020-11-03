package com.tanaguru.domain.exception;

public class CustomForbiddenException extends RuntimeException {
    
    private String[] content;
    
    public CustomForbiddenException() {
        super();
    }
    
    public CustomForbiddenException(String message) {
        super(message);
    }
    
    public CustomForbiddenException(String message, String[] content) {
        super(message);
        this.content = content;
    }
    
    public CustomForbiddenException(String error, long[] content) {
        super(error);
        this.content = new String[content.length];
        for(int i=0; i< content.length; i++) {
            this.content[i] = String.valueOf(content[i]);
        }
    }
    
    public String[] getContent() {
        return content;
    }
}
