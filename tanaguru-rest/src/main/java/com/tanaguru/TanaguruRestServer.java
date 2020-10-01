package com.tanaguru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author rcharre
 */
@SpringBootApplication
public class TanaguruRestServer {
    public static void main(String[] args) {
        SpringApplication.run(TanaguruRestServer.class, args);
    }
}
