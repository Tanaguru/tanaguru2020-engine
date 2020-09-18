package com.tanaguru.config;

import com.tanaguru.security.*;
import com.tanaguru.service.TanaguruUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.inject.Inject;

@Configuration
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final TanaguruUserDetailsService userDetailsService;
    private final AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;
    private final AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;
    private final Http401UnauthorizedEntryPoint authenticationEntryPoint;

    @Autowired
    public SecurityConfig(
            TanaguruUserDetailsService userDetailsService,
            AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler,
            AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler,
            Http401UnauthorizedEntryPoint authenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.ajaxAuthenticationSuccessHandler = ajaxAuthenticationSuccessHandler;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.ajaxAuthenticationFailureHandler = ajaxAuthenticationFailureHandler;
        this.ajaxLogoutSuccessHandler = ajaxLogoutSuccessHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .exceptionHandling()
                .accessDeniedHandler(new CustomAccessDeniedHandler())
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .formLogin()
                .loginProcessingUrl("/authentication")
                .successHandler(ajaxAuthenticationSuccessHandler)
                .failureHandler(ajaxAuthenticationFailureHandler)
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(ajaxLogoutSuccessHandler)
                .deleteCookies("JSESSIONID")
                .permitAll()
                .and()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .authorizeRequests()
                .antMatchers("/authentication").permitAll()
                .antMatchers("/logout").permitAll()
                .antMatchers("/users/forgot-password/**").permitAll()
                .antMatchers("/v2/api-docs",
                        "/configuration/ui",
                        "/swagger-resources/**",
                        "/configuration/security",
                        "/swagger-ui.html",
                        "/webjars/**").permitAll();
    }

    @Inject
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);
    }
}
