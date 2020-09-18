package com.tanaguru.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

@Component
public class PropertyConfig {
    public static String cryptoKey;

    @Value("${crypto.key}")
    public void setStaticCryptoKey(String cryptoKey){
        PropertyConfig.cryptoKey = cryptoKey;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
