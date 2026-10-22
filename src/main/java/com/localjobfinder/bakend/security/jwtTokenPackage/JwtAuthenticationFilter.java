package com.localjobfinder.bakend.security.jwtTokenPackage;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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
                                   @NotNull HttpServletResponse response,
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
                                   List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                }
            }

        } catch (Exception ex) {
            // unexpected exception during auth filter
            try {
                throw new Exception("Token validation failed");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        filterChain.doFilter(request, response);
    }


//    private void writeErrorResponse(AuthResponse authResponse, HttpServletResponse response) throws IOException {
//        response.setStatus(authResponse.getStatus());
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//
//        String json = mapper.writeValueAsString(authResponse);
//        response.getWriter().write(json);
//        response.getWriter().flush();
//    }
}
