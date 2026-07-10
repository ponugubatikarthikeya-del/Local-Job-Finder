package com.localjobfinder.bakend.security.jwtTokenPackage;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.AuthResponse;
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

                    // set a minimal authority - roles can be added to JWT claims later
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (CustomException ex) {
            AuthResponse authResponse = new AuthResponse(
                    ex.getStatus(),
                    ex.getErrorCode().name(),
                    ex.getErrorCode().getMessage(),
                    SecurityUtils.sanitize(request.getRequestURI())
            );

            writeErrorResponse(authResponse, response);
            return;
        } catch (Exception ex) {
            // unexpected exception during auth filter
            AuthResponse authResponse = new AuthResponse(
                    401,
                    "TOKEN_INVALID",
                    "Token validation failed",
                    SecurityUtils.sanitize(request.getRequestURI())
            );
            writeErrorResponse(authResponse, response);
            return;
        }

        filterChain.doFilter(request, response);
    }


    private void writeErrorResponse(AuthResponse authResponse, HttpServletResponse response) throws IOException {
        // Set status and content type
        response.setStatus(authResponse.getStatus());
        response.setContentType("application/json");

        // Write JSON using the class-level pre-configured mapper
        String json = mapper.writeValueAsString(authResponse);
        response.getWriter().write(json);
    }
}
