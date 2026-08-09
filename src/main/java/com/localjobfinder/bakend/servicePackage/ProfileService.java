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
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.repository.JobsRepository;
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
    private JobsRepository jobsRepository;


    @Autowired
    public void setUserRepository(UserRepository userRepository, ModelMapper modelMapper, JobsRepository jobsRepository)
    {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.jobsRepository = jobsRepository;
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
    public MyProfileResponse getPublicProfile(Long jobId, String path) throws CustomException {

        if(jobId == null){
            throw new CustomException(ErrorMsg.AUTH_004,400);
        }

        Optional<JobsEntity> jobsEntity = jobsRepository.findById(jobId);

        if(jobsEntity.isEmpty()){
            throw new CustomException(ErrorMsg.BUS_008,404);
        }

        JobsEntity job = jobsEntity.get();

        Optional<UserEntity> userEntity = userRepository.findById(job.getUser().getId());

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

        if (userId == null) {
            throw new CustomException(ErrorMsg.AUTH_004, 400);
        }

        // Use a clean custom exception if user isn't found
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008, 400));

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


//    @Override
//    @Transactional
//    public ProfileResponse updateProfile(Long userId, String path, EditProfileRequest request) throws CustomException {
//
//        if(userId == null){
//            throw new CustomException(ErrorMsg.AUTH_004,400);
//        }
//
//        Optional<UserEntity> userEntity = userRepository.findById(userId);
//
//        if(userEntity.isEmpty()){
//            throw new CustomException(ErrorMsg.AUTH_008,400);
//        }
//
//        UserEntity user = userEntity.get();
//
//        // 2. Dynamically apply updates to the parent UserEntity
//        if (request.getUsername() != null && !request.getUsername().isBlank()) {
//            user.setUsername(request.getUsername());
//        }
//        if (request.getPhone() != null && !request.getPhone().isBlank()) {
//            user.setPhone(request.getPhone());
//        }
//
//        // 3. Extract the linked profile entity
//        ProfilesEntity profile = user.getProfile();
//
//        if (profile == null) {
//            // Defensive check: If no profile exists for some reason, instantiate a fresh one linked to the user
//            profile = new ProfilesEntity();
//            profile.setUser(user);
//            user.setProfile(profile);
//        }
//
//        // 4. Dynamically apply updates to the child ProfilesEntity
//        if (request.getHeadline() != null) {
//            profile.setHeadline(request.getHeadline());
//        }
//        if (request.getSkills() != null) {
//            profile.setSkills(request.getSkills());
//        }
//        if (request.getCityName() != null) {
//            profile.setCityName(request.getCityName());
//        }
//        if (request.getUserDescription() != null) {
//            profile.setUserDescription(request.getUserDescription());
//        }
//        if(request.getDateOfBirth() != null){
//            profile.setDateOfBirth(request.getDateOfBirth());
//        }
//
//        // 5. Commit state. CascadeType.ALL handles propagating updates down to the profile record automatically.
//        userRepository.save(user);
//
//        return new ProfileResponse(
//                201,
//                SuccessMsg.USER_202.name(),
//                SuccessMsg.USER_202.getMessage(),
//                path
//        );
//    }

    @Override
    @Transactional
    public ProfileResponse updateUserAddress(Long id, AddressTakeRequest addressTakeRequest, String request) throws CustomException {

        if(id == null){
            throw new CustomException(ErrorMsg.AUTH_004,400);
        }

        if (addressTakeRequest == null) {
            throw new CustomException(ErrorMsg.AUTH_008,400);
        }

        Optional<UserEntity> userEntity = userRepository.findById(id);

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_008,400);
        }

        UserEntity user = userEntity.get();

        ProfilesEntity profilesEntity = user.getProfile();

        if(profilesEntity == null){
            throw new CustomException(ErrorMsg.AUTH_008,400);
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
