package com.tanaguru.domain.exception;

import com.tanaguru.domain.constant.CustomError;

public class CustomInvalidEntityException extends RuntimeException {
    
    private String[] content;
    
    public CustomInvalidEntityException() {
        super();
    }
    
    public CustomInvalidEntityException(CustomError error) {
        super(error.toString());
    }
    
    public CustomInvalidEntityException(CustomError error, String... content) {
        super(error.toString());
        this.content = content;
    }
    
    public CustomInvalidEntityException(CustomError error, long... content) {
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
