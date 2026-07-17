package com.localjobfinder.bakend.controllerPackage;


import com.localjobfinder.bakend.apiDataPackage.FcmTokenRequest;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.AuthResponse;
import com.localjobfinder.bakend.security.SecurityService;
import com.localjobfinder.bakend.servicePackage.FcmTokenService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/fcm/")
public class FcmTokenController {

    private static final Logger log = LoggerFactory.getLogger(FcmTokenController.class);

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    private final FcmTokenService fcmTokenService;

    private final SecurityService securityService;

    @Autowired
    public FcmTokenController(FcmTokenService fcmTokenService,SecurityService securityService){
        this.fcmTokenService = fcmTokenService;
        this.securityService = securityService;
    }

    @PutMapping("fcm-token/register")
    public ResponseEntity<AuthResponse> RegisterFcmToken(
            @Valid @RequestBody FcmTokenRequest tokenRequest,
            HttpServletRequest httpServletRequest
    ) throws CustomException {

        Long currentUserId = securityService.getCurrentUserId();

        log.info("updateFcmToken api called for email={}",currentUserId);

        // Call service to update the token
        AuthResponse response = fcmTokenService.updateFcmToken(currentUserId, tokenRequest.getDeviceFcmToken(),getPath(httpServletRequest));

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logoutToken(
            @RequestBody FcmTokenRequest request,
            HttpServletRequest httpServletRequest) throws CustomException {

        Long currentUserId = securityService.getCurrentUserId();

        log.info("logoutFcmToken api called for email={}",currentUserId);

        AuthResponse authResponse = fcmTokenService
                .removeTokenOnLogout(currentUserId,request.getDeviceFcmToken(),getPath(httpServletRequest));

        return ResponseEntity.status(HttpStatusCode.valueOf(authResponse.getStatus())).body(authResponse);
    }

}
