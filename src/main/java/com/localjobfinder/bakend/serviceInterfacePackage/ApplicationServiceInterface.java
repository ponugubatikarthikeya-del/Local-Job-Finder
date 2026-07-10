package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.responsePackage.ViewApplicantsResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ApplicationServiceInterface {

    public JobResponse applyJob(String path, Long userId, Long jobId) throws CustomException;

    public ViewApplicantsResponse viewApplicants(String path, Long ownerId, Long jobId, Pageable pageable) throws CustomException;

    public JobResponse acceptApplicant(String path, Long ownerId, Long jobId, Long applicantId) throws CustomException;

    public JobResponse rejectApplicant(String path, Long ownerId, Long jobId, Long applicantId) throws CustomException;

}
