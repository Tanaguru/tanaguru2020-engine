package com.tanaguru.domain.exception;

public class CustomInvalidEntityException extends RuntimeException {
    
    private String[] content;
    
    public CustomInvalidEntityException() {
        super();
    }
    
    public CustomInvalidEntityException(String message) {
        super(message);
    }
    
    public CustomInvalidEntityException(String message, String[] content) {
        super(message);
        this.content = content;
    }
    
    public CustomInvalidEntityException(String error, long[] content) {
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
