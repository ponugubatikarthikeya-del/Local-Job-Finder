package com.localjobfinder.bakend.security;

import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

@Component
public class SecurityService {

    public Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorMsg.AUTH_004, 401, "Authentication required");
        }

        return Long.parseLong(authentication.getPrincipal().toString());
    }
}