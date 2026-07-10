package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.AllJobsResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface JobFilterServiceInterface {

    AllJobsResponse searchJobs(BigDecimal lat, BigDecimal lng, Double radius, String search,
                               String category, String path, Long userId, Pageable pageable) throws CustomException;

    public AllJobsResponse getActiveJobs(BigDecimal lat, BigDecimal lng, Double radius, String request, Long userId, Pageable pageable) throws CustomException;

    public AllJobsResponse getMyJobs(String path, Long userId,Pageable pageable) throws CustomException;
}
