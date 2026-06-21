package com.localjobfinder.bakend.jwtTokenPackage;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.ApiResponse;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

//   WebMvcProperties.Apiversion.Use a single, properly configured mapper for the whole class

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // get header

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            String userId = jwtService.extractUserId(token);

            //validate the token

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.validateToken(token, userId)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.emptyList()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (CustomException ex) {
            // 1. Create the ApiResponse object using the exception data
            ApiResponse apiResponse = new ApiResponse(
                    ex.getStatus(),
                    ex.getErrorCode().name(),
                    ex.getErrorCode().getMessage(),
                    SecurityUtils.sanitize(request.getRequestURI())
            );

            // 2. Pass the response and the apiResponse object to the helper
            writeErrorResponse(apiResponse, response);
            return;
        }

        filterChain.doFilter(request, response);
    }


    private void writeErrorResponse(ApiResponse apiResponse, HttpServletResponse response) throws IOException {
        // Set status and content type
        response.setStatus(apiResponse.getStatus());
        response.setContentType("application/json");

        // Write JSON using the class-level pre-configured mapper
        String json = mapper.writeValueAsString(apiResponse);
        response.getWriter().write(json);
    }
}
