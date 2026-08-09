package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.dbEntity.FcmTokenEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.repository.FcmTokenRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import com.localjobfinder.bakend.responsePackage.AuthResponse;
import com.localjobfinder.bakend.serviceInterfacePackage.FcmServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class FcmTokenService implements FcmServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(FcmTokenService.class.getName());

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    @Autowired
    public FcmTokenService(FcmTokenRepository fcmTokenRepository, UserRepository userRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AuthResponse updateFcmToken(Long userId, String fcmToken, String request) throws CustomException {
        // 1. Fail-fast validation

        if (userId == null) {
            throw new CustomException(ErrorMsg.AUTH_008, 400);
        }

        if (fcmToken == null || fcmToken.isBlank() || request == null) {
            throw new CustomException(ErrorMsg.TOKEN_002, 400);
        }

//        💡 FIX: Purge old FCM rows for this user right before inserting the fresh one
        fcmTokenRepository.deleteByUserId(userId);

        // 2. Safe delete: clear this specific token if it was previously tied to any user
        fcmTokenRepository.deleteByDeviceToken(fcmToken);


        // 3. FORCE HIBERNATE TO SYNC WITH DATABASE RIGHT NOW (Fixes Error 1062)
        fcmTokenRepository.flush();

        // 3. Prevent heavy SELECT query using lazy evaluation proxy
        UserEntity userReference = userRepository.getReferenceById(userId);

        // 4. Build and save the new relation entry cleanly
        FcmTokenEntity fcmTokenEntity = new FcmTokenEntity();
        fcmTokenEntity.setUser(userReference);
        fcmTokenEntity.setDeviceToken(fcmToken);
        fcmTokenEntity.setUpdatedAt(Instant.now());

        fcmTokenRepository.save(fcmTokenEntity);
        log.info("Successfully linked FCM token to userId={}", userId);


//        UserEntity user = userRepository.findById(userId)
//                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008,400));
//
//        if(fcmToken == null || fcmToken.isBlank() || request == null){
//            throw new CustomException(ErrorMsg.TOKEN_002,400);
//        }
//
//        fcmTokenRepository.deleteByDeviceToken(fcmToken);
//
//        FcmTokenEntity fcmTokenEntity = new FcmTokenEntity();
//
//        fcmTokenEntity.setUser(user);
//        fcmTokenEntity.setDeviceToken(fcmToken);
//
//        fcmTokenRepository.save(fcmTokenEntity);
//
//        log.info("Successfully updated reactive FCM device token for userId={}", userId);


        return new AuthResponse(
                200,
                SuccessMsg.AUTH_202.name(),
                SuccessMsg.AUTH_202.getMessage(),
                request
        );
    }

//    @Override
//    @Transactional
//    public AuthResponse removeTokenOnLogout(Long currentUserId, String deviceFcmToken, String path) throws CustomException {
//
//        userRepository.findById(currentUserId)
//                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008,400));
//
//        if (deviceFcmToken == null || deviceFcmToken.isBlank() || path == null) {
//            throw new CustomException(ErrorMsg.TOKEN_002, 400);
//        }
//
//       fcmTokenRepository.deleteByUserIdAndDeviceToken(currentUserId, deviceFcmToken);
//
//        log.info("Successfully removed FCM token on logout for userId={}", currentUserId);
//
//        return new AuthResponse(
//                200,
//                SuccessMsg.AUTH_202.name(),
//                SuccessMsg.AUTH_202.getMessage(),
//                path
//        );
//    }
}
