package com.tanaguru.domain.exception;

import javax.persistence.EntityNotFoundException;

public class CustomEntityNotFoundException extends EntityNotFoundException{
    
    private static final long serialVersionUID = 1L;
    private String content;
    
    public CustomEntityNotFoundException() {
        super();
    }
    
    public CustomEntityNotFoundException(String error) {
        super(error);
    }
    
    public CustomEntityNotFoundException(String error, String content) {
        super(error);
        this.content = content;
    }
    
    public CustomEntityNotFoundException(String error, long content) {
        super(error);
        this.content = String.valueOf(content);
       
    }
    
    public String getContent() {
        return content;
    }
}
