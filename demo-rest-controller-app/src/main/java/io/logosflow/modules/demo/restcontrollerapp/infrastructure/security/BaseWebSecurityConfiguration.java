package io.logosflow.modules.demo.restcontrollerapp.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class BaseWebSecurityConfiguration {

    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers().frameOptions().disable()
                .httpStrictTransportSecurity().disable()
                .and()
                .authorizeHttpRequests(requestCustomizer -> requestCustomizer
                        .anyRequest().permitAll()
                );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        configure(http);
        return http.build();
    }
}
