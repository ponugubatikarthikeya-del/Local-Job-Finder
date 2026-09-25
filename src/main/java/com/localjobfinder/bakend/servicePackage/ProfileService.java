package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.apiDataPackage.AddressTakeRequest;
import com.localjobfinder.bakend.apiDataPackage.EditProfileRequest;
import com.localjobfinder.bakend.dbEntity.JobsEntity;
import com.localjobfinder.bakend.dbEntity.ProfilesEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.dtoPackage.PublicProfileDto;
import com.localjobfinder.bakend.dtoPackage.UserDto;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.InvalidException;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.ResourceNotFoundException;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.UnauthorizedException;
import com.localjobfinder.bakend.repository.JobsRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import com.localjobfinder.bakend.responsePackage.MyProfileResponse;
import com.localjobfinder.bakend.responsePackage.ProfileResponse;
import com.localjobfinder.bakend.serviceInterfacePackage.ProfileServiceInterface;

import com.localjobfinder.bakend.utilsPackage.MapStructMapper;
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
    private MapStructMapper mapStructMapper;
    private JobsRepository jobsRepository;


    @Autowired
    public void setUserRepository(UserRepository userRepository, MapStructMapper mapStructMapper, JobsRepository jobsRepository)
    {
        this.userRepository = userRepository;
        this.mapStructMapper = mapStructMapper;
        this.jobsRepository = jobsRepository;
    }

    // get user details by path variable user id

    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long userId, String path) {

        if(userId == null){
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if(userEntity.isEmpty()){
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        UserEntity user = userEntity.get();

//        // map entity to DTO using mapStruct
        ProfilesEntity profile = user.getProfile();

        if(profile == null){
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        UserDto userDto = mapStructMapper.toUserDtoByUserEntityAndProfileEntity(user, profile);

//        userDto.setId(user.getId());
//        userDto.setUsername(user.getUsername());
//        userDto.setEmail(user.getEmail());
//        userDto.setPhone(user.getPhone());
//        userDto.setCreatedAt(user.getCreatedAt());
//        userDto.setHeadline(profilesEntity.getHeadline());
//        userDto.setSkills(profilesEntity.getSkills());
//        userDto.setDateOfBirth(profilesEntity.getDateOfBirth());
//        userDto.setCityName(profilesEntity.getCityName());
//        userDto.setRating(profilesEntity.getRating());
//        userDto.setTotalJobsCompleted(profilesEntity.getTotalJobsCompleted());
//        userDto.setUserDescription(profilesEntity.getUserDescription());

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
    public MyProfileResponse getPublicProfile(Long jobId, String path) {

        if(jobId == null){
            throw new InvalidException(ErrorMsg.AUTH_004.getMessage());
        }

        Optional<JobsEntity> jobsEntity = jobsRepository.findById(jobId);

        if(jobsEntity.isEmpty()){
            throw new ResourceNotFoundException(ErrorMsg.BUS_008.getMessage());
        }

        JobsEntity job = jobsEntity.get();

        Optional<UserEntity> userEntity = userRepository.findById(job.getUser().getId());

        if(userEntity.isEmpty()){
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        UserEntity user = userEntity.get();

        ProfilesEntity profilesEntity = user.getProfile();


        if(profilesEntity == null){
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        PublicProfileDto profileDto = mapStructMapper.
                toPPDtoByUserEntityAndPublicProfileEntity(user, profilesEntity);

//        profileDto.setId(profilesEntity.getId());
//        profileDto.setUsername(userEntity.get().getUsername());
//        profileDto.setHeadline(profilesEntity.getHeadline());
//        profileDto.setSkills(profilesEntity.getSkills());
//        profileDto.setCityName(profilesEntity.getCityName());
//        profileDto.setRating(profilesEntity.getRating());
//        profileDto.setTotalJobsCompleted(profilesEntity.getTotalJobsCompleted());
//        profileDto.setUserDescription(profilesEntity.getUserDescription());

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
    public ProfileResponse updateProfile(Long userId, String path, EditProfileRequest request) {

        if (userId == null) {
            throw new UnauthorizedException(ErrorMsg.AUTH_004.getMessage());
        }

        // Use a clean custom exception if user isn't found
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage()));

        // 2. Dynamically apply updates to the parent UserEntity
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        // 3. Extract the linked profile entity (JPA will now load the existing row correctly)
        ProfilesEntity profile = user.getProfile();

        if (profile == null) {
            profile = new ProfilesEntity();
            user.setProfile(profile); // Triggers our helper to link both sides
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
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }

        // NOTE: userRepository.save(user) removed. @Transactional handles the UPDATE query automatically.

        return new ProfileResponse(
                200, // Changed to 200 OK since we are modifying an existing profile, not creating one (201)
                SuccessMsg.USER_202.name(),
                SuccessMsg.USER_202.getMessage(),
                path
        );
    }

    @Override
    @Transactional
    public ProfileResponse updateUserAddress(Long id, AddressTakeRequest addressTakeRequest, String request)  {

        if(id == null){
            throw new UnauthorizedException(ErrorMsg.AUTH_004.getMessage());
        }

        if (addressTakeRequest == null) {
            throw new InvalidException("addressTakeRequest is null");
        }

        Optional<UserEntity> userEntity = userRepository.findById(id);

        if(userEntity.isEmpty()){
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        UserEntity user = userEntity.get();

        ProfilesEntity profilesEntity = user.getProfile();

        if(profilesEntity == null){
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        profilesEntity.setCityName(addressTakeRequest.getCityName());
        profilesEntity.setLatitude(addressTakeRequest.getLatitude());
        profilesEntity.setLongitude(addressTakeRequest.getLongitude());
        userRepository.save(user);

        return new ProfileResponse(
                201,
                SuccessMsg.USER_202.name(),
                SuccessMsg.USER_202.getMessage(),
                request
        );
    }
}
