package com.localjobfinder.bakend.serviceInterfacePackage;


import com.localjobfinder.bakend.apiDataPackage.SubmitReviewRequest;
import com.localjobfinder.bakend.responsePackage.JobResponse;

public interface ReviewServiceInterface {

    public JobResponse submitReview(Long userId, SubmitReviewRequest submitReviewRequest, String path);
}
