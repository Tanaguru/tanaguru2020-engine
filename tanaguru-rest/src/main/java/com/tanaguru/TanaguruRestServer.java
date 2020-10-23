package com.tanaguru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * @author rcharre
 */

@EnableRetry
@SpringBootApplication
public class TanaguruRestServer {
    public static void main(String[] args) {
        SpringApplication.run(TanaguruRestServer.class, args);
    }
}
