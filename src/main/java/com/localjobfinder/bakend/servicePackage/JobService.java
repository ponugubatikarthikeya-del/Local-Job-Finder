package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.apiDataPackage.PostJobRequest;
import com.localjobfinder.bakend.components.GeometryUtils;
import com.localjobfinder.bakend.converterPackage.JobDto;
import com.localjobfinder.bakend.dbEntity.ActiveJobsListEntity;
import com.localjobfinder.bakend.dbEntity.PostJobsEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.JobStatusEnum;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.repository.ActiveJobsListRepository;
import com.localjobfinder.bakend.repository.JobsRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import com.localjobfinder.bakend.responsePackage.UserResponse;
import com.localjobfinder.bakend.serviceInterfacePackage.JobServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JobService implements JobServiceInterface {

    private final JobsRepository jobsRepository;
    private final UserRepository userRepository;
    private final ActiveJobsListRepository  activeJobsListRepository;
   private final GeometryUtils geometryUtils;
   private final org.modelmapper.ModelMapper modelMapper;

    @Autowired
    public JobService(JobsRepository jobsRepository, UserRepository userRepository
    , GeometryUtils geometryUtils, ActiveJobsListRepository activeJobsListRepository, org.modelmapper.ModelMapper modelMapper) {
        this.jobsRepository = jobsRepository;
        this.userRepository = userRepository;
        this.geometryUtils = geometryUtils;
        this.activeJobsListRepository = activeJobsListRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public UserResponse postJob(PostJobRequest postJobRequest, String path, String userIdStr) throws CustomException {

        if(userIdStr == null){
            throw new CustomException(ErrorMsg.AUTH_004,400,"userId is null");
        }

        Long userId = Long.parseLong(userIdStr);

        Optional<UserEntity> user = userRepository.findById(userId);

        if(user.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_008,404,"user not found");
        }

        UserEntity userEntity = user.get();

        PostJobsEntity postJob = new PostJobsEntity();

        postJob.setTitle(postJobRequest.getTitle());
        postJob.setDescription(postJobRequest.getDescription());
        postJob.setCategory(postJobRequest.getCategory());
        postJob.setPay_amount(postJobRequest.getPayAmount());
        postJob.setJobType(postJobRequest.getJobType());
        postJob.setLatitude(postJobRequest.getLatitude());
        postJob.setLongitude(postJobRequest.getLongitude());
        postJob.setAddress(postJobRequest.getAddress());
        postJob.setJobTime(postJobRequest.getJobTime());
        postJob.setStatus(JobStatusEnum.OPEN);

        postJob.setUser(userEntity);

        PostJobsEntity savedJob = jobsRepository.save(postJob);

        ActiveJobsListEntity listing = new ActiveJobsListEntity();

        listing.setJob(savedJob);
        listing.setOwner(userEntity);
        listing.setLocation(geometryUtils.createPoint(postJobRequest.getLatitude(),postJobRequest.getLongitude()));
        listing.setPayAmount(postJobRequest.getPayAmount());
        listing.setCategory(postJobRequest.getCategory());
        listing.setExpiryAt(LocalDateTime.now().plusDays(7));

        activeJobsListRepository.save(listing);

        // return a DTO or sanitized entity (keep minimal change: return saved job)
        // map to DTO
        JobDto dto = modelMapper.map(savedJob, com.localjobfinder.bakend.converterPackage.JobDto.class);
        dto.setUserId(userEntity.getId());

       return new UserResponse(
              201,
              SuccessMsg.JOB_201.name(),
              SuccessMsg.JOB_201.getMessage(),
              path,
              dto
       );

    }

//    @Transactional(readOnly = true)
//    public UserResponse getJobs(String path, String userIdStr) throws CustomException {
//
//        if(userIdStr == null){
//            throw new CustomException(ErrorMsg.AUTH_004,400,"userId is null");
//        }
//
//        Long userId = Long.parseLong(userIdStr);
//        Optional<UserEntity> user = userRepository.findById(userId);
//
//        if(user.isEmpty()){
//            throw new CustomException(ErrorMsg.AUTH_008,404,"user not found");
//        }
//
//        UserEntity userEntity = user.get();
//
//        List<PostJobsEntity> jobsList = userEntity.getJobs();
//
//        // map to DTO list
//        java.util.List<com.localjobfinder.bakend.converterPackage.JobDto> dtoList = new java.util.ArrayList<>();
//        for (PostJobsEntity j : jobsList) {
//            com.localjobfinder.bakend.converterPackage.JobDto dto = modelMapper.map(j, com.localjobfinder.bakend.converterPackage.JobDto.class);
//            dto.setUserId(userEntity.getId());
//            dtoList.add(dto);
//        }
//
//        return new UserResponse(
//                200,
//                SuccessMsg.JOB_200.name(),
//                SuccessMsg.JOB_200.getMessage(),
//                path,
//                dtoList
//        );
//    }
//
//    // apply job
//
//
//    @Override
//    public UserResponse applyJob(String request, String userIdStr,Long jobId) throws CustomException {
//
//        if(userIdStr== null){
//            throw new CustomException(ErrorMsg.AUTH_004,400,"userId is null");
//        }
//
//        Long userId;
//        try {
//            userId = Long.parseLong(userIdStr);
//        } catch (NumberFormatException ex) {
//            throw new CustomException(ErrorMsg.AUTH_004,400,"invalid userId format");
//        }
//
//        UserEntity user = userRepository.findById(userId)
//                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008,404,"user not found"));
//
//        if(jobId == null){
//            throw new CustomException(ErrorMsg.AUTH_008,400,"jobId is null");
//        }
//
//        ActiveJobsListEntity listing = activeJobsListRepository.findById(jobId)
//                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008,404,"job not found"));
//
//        // Simple application flow: ensure listing not expired
//        if (listing.getExpiryAt() != null && listing.getExpiryAt().isBefore(LocalDateTime.now())) {
//            throw new CustomException(ErrorMsg.JOB_404,400,"listing expired");
//        }
//
//        // TODO: create ApplicationJobsEntity and persist application record. Minimal change: acknowledge application.
//        return new UserResponse(
//                200,
//                SuccessMsg.JOB_200.name(),
//                "Applied successfully",
//                request,
//                null
//        );
//    }
}
