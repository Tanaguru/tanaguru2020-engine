package com.tanaguru.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tanaguru.handler.LimitLoginAuthenticationProvider;
import com.tanaguru.security.Http401UnauthorizedEntryPoint;
import com.tanaguru.security.Oauth2JwtRequestFilter;
import com.tanaguru.service.TanaguruUserDetailsService;

@ConditionalOnExpression("${oauth2.auth.enabled}")
@Configuration
@Order(101) // default order 100
public class SecurityConfig2 extends WebSecurityConfigurerAdapter {
    private final TanaguruUserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Http401UnauthorizedEntryPoint authenticationEntryPoint;
    private final LimitLoginAuthenticationProvider limitLoginAuthenticationProvider;
    private final Oauth2JwtRequestFilter oauth2JwtRequestFilter;

    @Value("${oauth2.auth.enabled}")
    private boolean oauth2AuthEnabled;

    @Autowired
    public SecurityConfig2(TanaguruUserDetailsService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder,
            Http401UnauthorizedEntryPoint authenticationEntryPoint,
            LimitLoginAuthenticationProvider limitLoginAuthenticationProvider,
            Oauth2JwtRequestFilter oauth2JwtRequestFilter) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.limitLoginAuthenticationProvider = limitLoginAuthenticationProvider;
        this.oauth2JwtRequestFilter = oauth2JwtRequestFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests().antMatchers("/logout").permitAll()
                .antMatchers("/users/forgot-password/**").permitAll()
                .antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/security",
                        "/swagger-ui.html", "/webjars/**")
                .permitAll().and().exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(oauth2JwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    }

}
