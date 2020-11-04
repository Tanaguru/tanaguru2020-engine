package com.tanaguru.domain.exception;

import com.tanaguru.domain.constant.CustomError;

public class CustomIllegalStateException extends IllegalStateException{

    private String[] content;
    
    public CustomIllegalStateException() {
        super();
    }
    
    public CustomIllegalStateException(CustomError error) {
        super(error.toString());
    }
    
    public CustomIllegalStateException(CustomError error, String... content) {
        super(error.toString());
        this.content = content;
    }
    
    public CustomIllegalStateException(CustomError error, long... content) {
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
