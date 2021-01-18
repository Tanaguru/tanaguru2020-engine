package com.tanaguru.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
 
@Service
public class MessageService {
 
    @Autowired
    private MessageSource messageSource;
 
    public String getMessage(String code) {
    	//messageSource.setDefaultEncoding("UTF-8");
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
