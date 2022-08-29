package com.tanaguru.config;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

@Configuration
public class AccessibilityScriptConfig {

    @Value("scripts/accessibilityScript.js")
    private ClassPathResource accessibilityScriptResource;

    @Bean(name = "accessibilityScript")
    public String accessibilityScript() throws IOException {
        return StreamUtils.copyToString(
                accessibilityScriptResource.getInputStream(),
                Charset.defaultCharset()
        );
    }
}
