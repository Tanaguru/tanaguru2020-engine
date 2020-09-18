package com.tanaguru.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;


@Configuration
public class CoreScriptConfig {

    @Value("scripts/content.js")
    private ClassPathResource coreScriptResource;

    @Bean(name = "coreScript")
    public String coreScript() throws IOException {
        return StreamUtils.copyToString(
                coreScriptResource.getInputStream(),
                Charset.defaultCharset()
        );
    }
}
