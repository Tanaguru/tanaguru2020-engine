package com.tanaguru.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class TanaguruMailConfig {

    @Bean
    public JavaMailSender mailSender() {
        return  new JavaMailSenderImpl();
    }
}
