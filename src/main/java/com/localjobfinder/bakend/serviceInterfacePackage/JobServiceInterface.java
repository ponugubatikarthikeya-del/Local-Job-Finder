package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.apiDataPackage.PostJobRequest;
import com.localjobfinder.bakend.responsePackage.*;

public interface JobServiceInterface {

    public UserResponse postJob(PostJobRequest postJobRequest, String request,
                                Long userId) ;

    public JobResponse deleteJob(Long userId, Long jobId, String path) ;

    public JobResponse jobComplete(Long userId, Long jobId, String path);

//    public JobDetailsResponse getJobDetails(String path, Long userId, Long jobId);

}