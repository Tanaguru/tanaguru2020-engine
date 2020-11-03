package com.tanaguru.domain.exception;

import org.openqa.selenium.InvalidArgumentException;

public class CustomInvalidArgumentException extends InvalidArgumentException {

    private String content[];
    
    public CustomInvalidArgumentException(String error) {
        super(error);
    }
    
    public CustomInvalidArgumentException(String error, String[] content) {
        super(error);
        this.content = content;
    }
    
    public CustomInvalidArgumentException(String error, long[] content) {
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
