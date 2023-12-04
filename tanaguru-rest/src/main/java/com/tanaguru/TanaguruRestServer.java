package com.tanaguru;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.tanaguru.config.SecurityConfig;

import java.util.Locale;

/**
 * @author rcharre
 */

@EnableRetry
@SpringBootApplication
@Import(SecurityConfig.class)
@EnableAsync
public class TanaguruRestServer {
    public static void main(String[] args) {
        SpringApplication.run(TanaguruRestServer.class, args);
    }
    
    @Value("${message.lang}")
    private String language;
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(getDefaultLocale());
        return slr;
    }
    
    @Bean("messageSource")
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(getDefaultLocale());
        return messageSource;
    }

    @Bean("threadPoolTaskExecutor")
    public TaskExecutor getAsyncExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(1000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("Async-");
        return executor;
    }
    
    private Locale getDefaultLocale() {
    	if(language.equals("fr")) {
    		return Locale.FRENCH;
    	} else {
    		return Locale.ENGLISH;
    	}
    }
}
