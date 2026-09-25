package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.dtoPackage.JobDto;
import com.localjobfinder.bakend.paging.JobFilterCriteria;
import com.localjobfinder.bakend.paging.JobPagingResponse;
import com.localjobfinder.bakend.paging.PagedResponse;

public interface JobFilterServiceInterface {

    public JobPagingResponse<PagedResponse<JobDto>> filterJobs(Long userId,
                                                               JobFilterCriteria criteria,String path);

//    AllJobsResponse searchJobs(BigDecimal lat, BigDecimal lng, Double radius, String search,
//                               String category, String path, Long userId, Pageable pageable) throws Exception;
//
//    AllJobsResponse getActiveJobs(BigDecimal lat, BigDecimal lng, Double radius, String request,
//                                  Long userId, Pageable pageable) throws Exception;

    public JobPagingResponse<PagedResponse<JobDto>> getMyJobs(Long userId,
                                                              String path, int page, int size) ;

//    public JobPagingResponse<PagedResponse<JobDto>> getFilterJobs(
//           Long userId, BigDecimal lat, BigDecimal lng, Double radius,
//            JobFilterCriteria jobFilterCriteria, int page, int size,String request
//    );
}
