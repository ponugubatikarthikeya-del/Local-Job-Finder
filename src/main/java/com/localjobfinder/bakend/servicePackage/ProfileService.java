package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.apiDataPackage.EditProfileRequest;
import com.localjobfinder.bakend.dbEntity.ProfilesEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.dtoPackage.PublicProfileDto;
import com.localjobfinder.bakend.dtoPackage.UserDto;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.repository.UserRepository;
import com.localjobfinder.bakend.responsePackage.AuthResponse;
import com.localjobfinder.bakend.responsePackage.MyProfileResponse;
import com.localjobfinder.bakend.responsePackage.ProfileResponse;
import com.localjobfinder.bakend.serviceInterfacePackage.ProfileServiceInterface;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileService implements ProfileServiceInterface {

    private final static Logger log =  LoggerFactory.getLogger(ProfileService.class);


    private UserRepository userRepository;
    private ModelMapper modelMapper;


    @Autowired
    public void setUserRepository(UserRepository userRepository, ModelMapper modelMapper)
    {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    // get user details by path variable user id

    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long userId, String path) throws CustomException {

        if(userId == null){
            throw new CustomException(ErrorMsg.AUTH_004,400,"userId is null");
        }

        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_008,404);
        }

        UserEntity user = userEntity.get();

//        // map entity to DTO using ModelMapper
//        UserDto dto = modelMapper.map(user, UserDto.class);

        ProfilesEntity profilesEntity = user.getProfile();

        if(profilesEntity == null){
            throw new CustomException(ErrorMsg.AUTH_008,400);
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setPhone(user.getPhone());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setHeadline(profilesEntity.getHeadline());
        userDto.setSkills(profilesEntity.getSkills());
        userDto.setDateOfBirth(profilesEntity.getDateOfBirth());
        userDto.setCityName(profilesEntity.getCityName());
        userDto.setRating(profilesEntity.getRating());
        userDto.setTotalJobsCompleted(profilesEntity.getTotalJobsCompleted());
        userDto.setUserDescription(profilesEntity.getUserDescription());

        return new MyProfileResponse(
                200,
                SuccessMsg.USER_200.name(),
                SuccessMsg.USER_200.getMessage(),
                path,
                userDto
        );
    }


    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse getPublicProfile(Long userId, String path) throws CustomException {

        if(userId == null){
            throw new CustomException(ErrorMsg.AUTH_004,400);
        }

        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_008,404);
        }

        ProfilesEntity profilesEntity = userEntity.get().getProfile();

        if(profilesEntity == null){
            throw new CustomException(ErrorMsg.AUTH_008,400);
        }

        PublicProfileDto profileDto = new PublicProfileDto();

        profileDto.setId(profilesEntity.getId());
        profileDto.setUsername(userEntity.get().getUsername());
        profileDto.setHeadline(profilesEntity.getHeadline());
        profileDto.setSkills(profilesEntity.getSkills());
        profileDto.setCityName(profilesEntity.getCityName());
        profileDto.setRating(profilesEntity.getRating());
        profileDto.setTotalJobsCompleted(profilesEntity.getTotalJobsCompleted());
        profileDto.setUserDescription(profilesEntity.getUserDescription());

        return new MyProfileResponse(
                200,
                SuccessMsg.USER_200.name(),
                SuccessMsg.USER_200.getMessage(),
                path,
                profileDto
        );
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(Long userId, String path, EditProfileRequest request) throws CustomException {

        if(userId == null){
            throw new CustomException(ErrorMsg.AUTH_004,400);
        }

        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_008,400);
        }

        UserEntity user = userEntity.get();

        // 2. Dynamically apply updates to the parent UserEntity
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        // 3. Extract the linked profile entity
        ProfilesEntity profile = user.getProfile();

        if (profile == null) {
            // Defensive check: If no profile exists for some reason, instantiate a fresh one linked to the user
            profile = new ProfilesEntity();
            profile.setUser(user);
            user.setProfile(profile);
        }

        // 4. Dynamically apply updates to the child ProfilesEntity
        if (request.getHeadline() != null) {
            profile.setHeadline(request.getHeadline());
        }
        if (request.getSkills() != null) {
            profile.setSkills(request.getSkills());
        }
        if (request.getCityName() != null) {
            profile.setCityName(request.getCityName());
        }
        if (request.getUserDescription() != null) {
            profile.setUserDescription(request.getUserDescription());
        }

        // 5. Commit state. CascadeType.ALL handles propagating updates down to the profile record automatically.
        userRepository.save(user);

        return new ProfileResponse(
                201,
                SuccessMsg.USER_202.name(),
                SuccessMsg.USER_202.getMessage(),
                path
        );
    }


    @Override
    @Transactional
    public AuthResponse updateFcmToken(Long userId, String fcmToken, String request) throws CustomException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008,400));

        if(fcmToken == null || fcmToken.isBlank() || request == null){
            throw new CustomException(ErrorMsg.TOKEN_002,400);
        }

        user.setFcmToken(fcmToken);
        userRepository.save(user);

        log.info("Successfully updated reactive FCM device token for userId={}", userId);
        return new AuthResponse(
                200,
                SuccessMsg.AUTH_202.name(),
                SuccessMsg.AUTH_202.getMessage(),
                request
        );
    }
}
