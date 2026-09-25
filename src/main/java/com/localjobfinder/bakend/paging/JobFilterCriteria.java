package com.localjobfinder.bakend.paging;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JobFilterCriteria {

    private String search;

    private String category;

    private Double salary;
//
//    private String location;
//
//    private String status;

    private BigDecimal lat;
    private BigDecimal lng;
    private Double radius;

    @Min(value = 0,message = "pages does not allow below zero")
    private int page;

    @Min(value = 10,message = "minimum page size is 10")
    @Max(value = 100,message = "maximum page size is 100")
    private int size = 20;

}
