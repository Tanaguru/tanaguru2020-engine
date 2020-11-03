package com.tanaguru.domain.exception;

import org.openqa.selenium.InvalidArgumentException;

public class CustomInvalidArgumentException extends InvalidArgumentException {

    private String content;
    
    public CustomInvalidArgumentException(String error) {
        super(error);
    }
    
    public CustomInvalidArgumentException(String error, String content) {
        super(error);
        this.content = content;
    }
    
    public CustomInvalidArgumentException(String error, long content) {
        super(error);
        this.content = String.valueOf(content);
        
    }
    
    public String getContent() {
        return content;
    }
}
