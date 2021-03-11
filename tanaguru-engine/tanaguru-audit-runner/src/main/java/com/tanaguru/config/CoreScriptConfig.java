package com.tanaguru.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    
    @Bean( name = "coreScriptVersion" )
    public String coreScriptVersion() throws IOException {
        String coreScript = StreamUtils.copyToString(
                coreScriptResource.getInputStream(),
                Charset.defaultCharset()
        );
        Pattern htmlPattern = Pattern.compile( "version=.*");
        Matcher htmlMatcher = htmlPattern.matcher( coreScript );
        String version = "version=1";
        while ( htmlMatcher.find() ) {
            version = htmlMatcher.group( 0 );
        }
        return version.split("=")[1];
    }  
}
