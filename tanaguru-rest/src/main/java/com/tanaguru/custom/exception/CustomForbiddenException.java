package com.tanaguru.custom.exception;

public class CustomForbiddenException extends RuntimeException {
    
    private String content;
    
    public CustomForbiddenException() {
        super();
    }
    
    public CustomForbiddenException(String message) {
        super(message);
    }
    
    public CustomForbiddenException(String message, String content) {
        super(message);
        this.content = content;
    }
    
    public CustomForbiddenException(String message, long content) {
        super(message);
        this.content = String.valueOf(content);
    }
    
    public String getContent() {
        return content;
    }
}
