package com.tanaguru.domain.exception;

import com.tanaguru.domain.constant.CustomError;

public class CustomForbiddenException extends RuntimeException {
    
    private String[] content;
    
    public CustomForbiddenException() {
        super();
    }
    
    public CustomForbiddenException(CustomError error) {
        super(error.toString());
    }
    
    public CustomForbiddenException(CustomError error, String... content) {
        super(error.toString());
        this.content = content;
    }
    
    public CustomForbiddenException(CustomError error, long... content) {
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
