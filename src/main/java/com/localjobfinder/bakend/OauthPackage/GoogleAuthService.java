package com.localjobfinder.bakend.OauthPackage;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    public GoogleIdToken.Payload verifyToken(String idTokenString)  {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            } else {
                throw new UnauthorizedException(ErrorMsg.OAUTH_001.getMessage());
            }
        } catch (Exception e) {
            throw new UnauthorizedException(ErrorMsg.OAUTH_002.getMessage());
        }
    }
}
