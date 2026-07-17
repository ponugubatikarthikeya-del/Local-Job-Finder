package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.apiDataPackage.EditProfileRequest;
import com.localjobfinder.bakend.apiDataPackage.FcmTokenRequest;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.AuthResponse;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.responsePackage.MyProfileResponse;
import com.localjobfinder.bakend.responsePackage.ProfileResponse;
import com.localjobfinder.bakend.security.SecurityService;
import com.localjobfinder.bakend.servicePackage.ProfileService;
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
@RequestMapping("user/profiles/")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);


    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    private ProfileService profileService;
    private SecurityService securityService;

    @Autowired
    public void setProfileService(ProfileService profileService, SecurityService securityService) {
        this.profileService = profileService;
        this.securityService = securityService;
    }


    @GetMapping("me")
    public ResponseEntity<MyProfileResponse> getMyProfile(HttpServletRequest httpServletRequest) throws CustomException {

        Long userId = securityService.getCurrentUserId();

        log.info("getUserDetails api called for id={}",userId);

        MyProfileResponse response = profileService.getMyProfile(userId, getPath(httpServletRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


    @GetMapping("public/{userId}")
    public ResponseEntity<MyProfileResponse> getPublicProfile(
            @PathVariable Long userId, HttpServletRequest httpServletRequest) throws CustomException {

        log.info("public profile api called for id={}",userId);

        MyProfileResponse response = profileService.getPublicProfile(userId, getPath(httpServletRequest));

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    @PatchMapping("update")
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody EditProfileRequest editProfileRequest,HttpServletRequest httpServletRequest
    ) throws CustomException {

        Long userId = securityService.getCurrentUserId();

        log.info("updateProfile api called for email={}",userId);


        ProfileResponse response = profileService.updateProfile(userId,getPath(httpServletRequest),editProfileRequest);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }


}
