package com.tanaguru.service.impl;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
 
@Service
public class MessageService {

    private final MessageSource messageSource;
    
    @Value("${message.lang}")
    private String language;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code) {
    	return messageSource.getMessage(code, null, getMessageLocale());
    }
    
    private Locale getMessageLocale() {
    	if(language.equals("fr")) {
    		return Locale.FRENCH;
    	} else {
    		return Locale.ENGLISH;
    	}
    }
}
