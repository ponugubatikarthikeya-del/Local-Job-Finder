package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.apiDataPackage.PostJobRequest;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface JobServiceInterface {

    public UserResponse postJob(PostJobRequest postJobRequest, String request,
                                Long userId) throws CustomException;

    public JobResponse deleteJob(Long userId, Long jobId, String path) throws CustomException;

    public JobResponse jobComplete(Long userId, Long jobId, String path) throws CustomException;

    public JobDetailsResponse getJobDetails(String path, Long userId, Long jobId) throws CustomException;

}