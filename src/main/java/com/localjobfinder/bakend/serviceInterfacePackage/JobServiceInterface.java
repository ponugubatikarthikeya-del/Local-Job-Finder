package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.apiDataPackage.PostJobRequest;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.UserResponse;

public interface JobServiceInterface {

    public UserResponse postJob(PostJobRequest postJobRequest, String request,
                                String userId) throws CustomException;

//    public UserResponse applyJob(String request,String usrId,Long jobId)  throws CustomException;
}
