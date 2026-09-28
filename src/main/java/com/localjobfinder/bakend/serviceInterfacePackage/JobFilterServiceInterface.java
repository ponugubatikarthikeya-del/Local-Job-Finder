package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.dtoPackage.JobDto;
import com.localjobfinder.bakend.apiDataPackage.JobFilterCriteria;
import com.localjobfinder.bakend.paging.JobPagingResponse;
import com.localjobfinder.bakend.paging.PagedResponse;

public interface JobFilterServiceInterface {

    public JobPagingResponse<PagedResponse<JobDto>> filterJobs(Long userId,
                                                               JobFilterCriteria criteria,String path);

    public JobPagingResponse<PagedResponse<JobDto>> getMyJobs(Long userId,
                                                              String path, int page, int size) ;

}
