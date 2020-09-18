package com.tanaguru;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class AuditRequestConsumerServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRequestConsumerServer.class);

    public static void main(String[] args) {
        SpringApplication.run(AuditRequestConsumerServer.class, args);
    }
}
