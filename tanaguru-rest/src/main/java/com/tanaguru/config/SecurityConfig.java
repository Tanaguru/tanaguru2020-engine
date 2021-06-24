package com.tanaguru.config;

import com.tanaguru.handler.LimitLoginAuthenticationProvider;
import com.tanaguru.security.Http401UnauthorizedEntryPoint;
import com.tanaguru.security.JwtRequestFilter;
import com.tanaguru.service.TanaguruUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;


@Configuration
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final TanaguruUserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Http401UnauthorizedEntryPoint authenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final LimitLoginAuthenticationProvider limitLoginAuthenticationProvider;

    @Autowired
    public SecurityConfig(
            TanaguruUserDetailsService userDetailsService,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            Http401UnauthorizedEntryPoint authenticationEntryPoint,
            JwtRequestFilter jwtRequestFilter, LimitLoginAuthenticationProvider limitLoginAuthenticationProvider) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
        this.limitLoginAuthenticationProvider = limitLoginAuthenticationProvider;
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setSameSite("none");
        return cookieSerializer;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/logout").permitAll()
                .antMatchers("/users/forgot-password/**").permitAll()
                .antMatchers("/v2/api-docs",
                        "/configuration/ui",
                        "/swagger-resources/**",
                        "/configuration/security",
                        "/swagger-ui.html",
                        "/webjars/**").permitAll()
                .and().exceptionHandling().
                authenticationEntryPoint(authenticationEntryPoint)
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(myAuthProvider());
    }

    @Bean
    public LimitLoginAuthenticationProvider myAuthProvider() throws Exception {
        limitLoginAuthenticationProvider.setPasswordEncoder(this.bCryptPasswordEncoder);
        limitLoginAuthenticationProvider.setUserDetailsService(this.userDetailsService);
        return limitLoginAuthenticationProvider;
    }

}
