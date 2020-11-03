package com.tanaguru.domain.exception;

public class CustomIllegalStateException extends IllegalStateException{

    private String[] content;
    
    public CustomIllegalStateException() {
        super();
    }
    
    public CustomIllegalStateException(String error, String[] content) {
        super(error);
        this.content = content;
    }
    
    public CustomIllegalStateException(String error, long[] content) {
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
