package com.tanaguru.domain.exception;

import com.tanaguru.domain.constant.CustomError;

import javax.persistence.EntityNotFoundException;

public class CustomEntityNotFoundException extends EntityNotFoundException{
    
    private static final long serialVersionUID = 1L;
    private String[] content;
    
    public CustomEntityNotFoundException() {
        super();
    }
    
    public CustomEntityNotFoundException(CustomError error) {
        super(error.toString());
    }
    
    public CustomEntityNotFoundException(CustomError error, String... content) {
        super(error.toString());
        this.content = content;
    }
    
    public CustomEntityNotFoundException(CustomError error, long... content) {
        super(error.toString());
        this.content = new String[content.length];
        for(int i=0; i< content.length; i++) {
            this.content[i] = String.valueOf(content[i]);
        }
    }
    
    public String[] getContent() {
        return content;
    }
}
