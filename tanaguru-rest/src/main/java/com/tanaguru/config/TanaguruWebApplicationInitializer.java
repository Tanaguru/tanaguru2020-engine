package com.tanaguru.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;

@Configuration
public class TanaguruWebApplicationInitializer implements WebApplicationInitializer {

    @Value("${spring.profiles.active}")
    private String profile;
    
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter("spring.profiles.active", profile);
    }
}