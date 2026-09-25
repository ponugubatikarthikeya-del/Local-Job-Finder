package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.dtoPackage.ApplicantDto;
import com.localjobfinder.bakend.dtoPackage.UserDto;
import com.localjobfinder.bakend.paging.JobPagingResponse;
import com.localjobfinder.bakend.paging.PagedResponse;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.responsePackage.ViewApplicantsResponse;
import org.springframework.data.domain.Pageable;

public interface ApplicationServiceInterface {

    public JobResponse applyJob(String path, Long userId, Long jobId) ;

    public JobPagingResponse<PagedResponse<ApplicantDto>> viewApplicants(String path, Long ownerId, Long jobId, int page, int size) ;

    public JobResponse acceptApplicant(String path, Long ownerId, Long jobId, Long applicantId);

    public JobResponse rejectApplicant(String path, Long ownerId, Long jobId, Long applicantId) ;

}
