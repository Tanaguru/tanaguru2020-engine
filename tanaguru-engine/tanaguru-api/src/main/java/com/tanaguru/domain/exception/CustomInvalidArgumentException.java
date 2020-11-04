package com.tanaguru.domain.exception;

import org.openqa.selenium.InvalidArgumentException;

import com.tanaguru.domain.constant.CustomError;

public class CustomInvalidArgumentException extends InvalidArgumentException {

    private String content[];
    
    public CustomInvalidArgumentException(CustomError error) {
        super(error.toString());
    }
    
    public CustomInvalidArgumentException(CustomError error, String... content) {
        super(error.toString());
        this.content = content;
    }
    
    public CustomInvalidArgumentException(CustomError error, long... content) {
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
