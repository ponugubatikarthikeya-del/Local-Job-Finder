package com.localjobfinder.bakend.utilsPackage;

import com.localjobfinder.bakend.apiDataPackage.PostJobRequest;
import com.localjobfinder.bakend.dbEntity.JobsEntity;
import com.localjobfinder.bakend.dbEntity.ProfilesEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.dtoPackage.ApplicantDto;
import com.localjobfinder.bakend.dtoPackage.JobDto;
import com.localjobfinder.bakend.dtoPackage.PublicProfileDto;
import com.localjobfinder.bakend.dtoPackage.UserDto;
import com.localjobfinder.bakend.repository.NearbyJobProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface MapStructMapper {

    JobDto toJobDtoByJobEntity(JobsEntity jobsEntity);

    @Mapping(source = "user.id", target = "id")
    UserDto toUserDtoByUserEntityAndProfileEntity(UserEntity user, ProfilesEntity profile);

    @Mapping(source = "profile.id",target = "id")
    PublicProfileDto toPPDtoByUserEntityAndPublicProfileEntity(UserEntity user, ProfilesEntity profile);

    JobsEntity toJobsEntityByPostJobRequest(PostJobRequest postJobRequest);

    @Mapping(source = "user.id",target = "userId")
    ApplicantDto toApplicantDtoByUserEntityAndProfileEntity(UserEntity user, ProfilesEntity profile);

    default Instant map(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }

}
