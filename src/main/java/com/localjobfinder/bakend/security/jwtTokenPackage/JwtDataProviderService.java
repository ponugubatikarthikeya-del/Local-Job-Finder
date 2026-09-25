package com.localjobfinder.bakend.security.jwtTokenPackage;

import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

@Component
public class JwtDataProviderService {

    public Long getCurrentUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException(ErrorMsg.AUTH_004.getMessage());
        }

        String username = authentication.getName();

        if(username.equals("anonymousUser")){
            throw new UnauthorizedException(ErrorMsg.AUTH_004.getMessage());
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof String)) {
            throw new UnauthorizedException("Unexpected principal type: " +
                    (principal == null ? "null" : principal.getClass().getName()));
        }


        String principalStr = (String) principal;

        // If principal is numeric ID as string

        try {

            return Long.parseLong(principalStr);

        } catch (NumberFormatException e) {

            throw new UnauthorizedException("Invalid user ID in token: " + principalStr);

        }
    }
}