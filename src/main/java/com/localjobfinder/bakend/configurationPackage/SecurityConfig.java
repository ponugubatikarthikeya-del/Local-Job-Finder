package com.localjobfinder.bakend.configurationPackage;

import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.jwtTokenPackage.JwtAuthenticationFilter;
import com.localjobfinder.bakend.jwtTokenPackage.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final JwtService jwtService;

    @Autowired
    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws CustomException {

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);

        http
                .csrf(csrf -> csrf.disable()) // REST API → disable CSRF

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                       // Public APIs - only authentication related endpoints
                       .requestMatchers(
                               "/user/signup",
                               "/user/login",
                               "/user/refresh",
                               "/user/forgot-password",
                               "/user/verify-otp",
                               "/user/change-password",
                               "/user/sample"
                       ).permitAll()

                       // Everything else → requires login
                       .anyRequest().authenticated()
                )

                // Add JWT filter before default auth filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


