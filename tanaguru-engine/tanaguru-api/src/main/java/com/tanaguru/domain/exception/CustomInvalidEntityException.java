package com.tanaguru.domain.exception;

public class CustomInvalidEntityException extends RuntimeException {
    
    private String content;
    
    public CustomInvalidEntityException() {
        super();
    }
    
    public CustomInvalidEntityException(String message) {
        super(message);
    }
    
    public CustomInvalidEntityException(String message, String content) {
        super(message);
        this.content = content;
    }
    
    public CustomInvalidEntityException(String message, long content) {
        super(message);
        this.content = String.valueOf(content);
    }
    
    public String getContent() {
        return content;
    }
}
