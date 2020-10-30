package com.tanaguru.custom.exception;

public class CustomIllegalStateException extends IllegalStateException{

    private String content;
    
    public CustomIllegalStateException() {
        super();
    }
    
    public CustomIllegalStateException(String error, String content) {
        super(error);
        this.content = content;
    }
    
    public CustomIllegalStateException(String error, long content) {
        super(error);
        this.content = String.valueOf(content);
    }
    
    public String getContent() {
        return content;
    }
}
