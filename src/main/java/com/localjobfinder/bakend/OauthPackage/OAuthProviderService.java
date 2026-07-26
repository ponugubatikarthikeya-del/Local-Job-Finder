package com.localjobfinder.bakend.OauthPackage;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;

@Service
public class OAuthProviderService {

    private final GoogleAuthService googleAuthService;
    private final RestTemplate restTemplate; // Injected to utilize proper connection pools

    @Autowired
    public OAuthProviderService(GoogleAuthService googleAuthService, RestTemplate restTemplate) {
        this.googleAuthService = googleAuthService;
        this.restTemplate = restTemplate;
    }

    public OAuthUserDto fetchOAuthUser(String provider, String token) throws CustomException {
        if (provider == null || token == null || token.isBlank()) {
            throw new CustomException(ErrorMsg.OAUTH_003, 400);
        }

        String normalizedProvider = provider.toUpperCase().trim();

        return switch (normalizedProvider) {
            case "GOOGLE" -> verifyGoogleUser(token);
            case "GITHUB" -> fetchGitHubUser(token);
            case "FACEBOOK" -> fetchFacebookUser(token);
            default -> throw new CustomException(ErrorMsg.OAUTH_004, 400);
        };
    }

    // 1. Google ID Token Verification
    private OAuthUserDto verifyGoogleUser(String idToken) throws CustomException {
        try {
            GoogleIdToken.Payload payload = googleAuthService.verifyToken(idToken);
            if (payload == null) {
                throw new CustomException(ErrorMsg.OAUTH_005, 401);
            }
            return new OAuthUserDto(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    AuthLoginEnum.GOOGLE
            );
        } catch (Exception e) {
            throw new CustomException(ErrorMsg.OAUTH_006, 401);
        }
    }

    // 2. GitHub Access Token Verification
    private OAuthUserDto fetchGitHubUser(String accessToken) throws CustomException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // Using ParameterizedTypeReference avoids unsafe runtime raw types casting checks
            ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            Map<String, Object> body = userResponse.getBody();
            if (body == null) throw new CustomException(ErrorMsg.OAUTH_007, 400);

            String githubId = String.valueOf(body.get("id"));
            String name = (String) body.get("name");

            // Fallback strategy if display name attribute is blank
            if (name == null || name.isBlank()) {
                name = (String) body.get("login");
            }

            String email = (String) body.get("email");

            if (email == null || email.isBlank()) {
                email = fetchGitHubPrimaryEmail(request);
            }

            if (email == null || email.isBlank()) {
                throw new CustomException(ErrorMsg.OAUTH_008, 400);
            }

            return new OAuthUserDto(githubId, email, name, AuthLoginEnum.GITHUB);
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorMsg.OAUTH_009, 401);
        }
    }

    private String fetchGitHubPrimaryEmail(HttpEntity<Void> request) {
        try {
            ResponseEntity<List<Map<String, Object>>> emailsResponse = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            List<Map<String, Object>> emailList = emailsResponse.getBody();
            if (emailList != null) {
                for (Map<String, Object> emailInfo : emailList) {
                    if (Boolean.TRUE.equals(emailInfo.get("primary"))) {
                        return (String) emailInfo.get("email");
                    }
                }
            }
        } catch (Exception ignored) {} // Fail gracefully and fall back to top validation exception layers
        return null;
    }

    // 3. Facebook Access Token Verification
    private OAuthUserDto fetchFacebookUser(String accessToken) throws CustomException {
        try {
            // Using Graph API versioned paths prevents unexpected protocol changes
            String url = "https://facebook.com" + accessToken;

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            Map<String, Object> body = response.getBody();
            if (body == null) throw new CustomException(ErrorMsg.OAUTH_007, 400);

            String fbId = (String) body.get("id");
            String name = (String) body.get("name");
            String email = (String) body.get("email");

            return new OAuthUserDto(fbId, email, name, AuthLoginEnum.FACEBOOK);
        } catch (Exception e) {
            throw new CustomException(ErrorMsg.OAUTH_011, 401);
        }
    }
}