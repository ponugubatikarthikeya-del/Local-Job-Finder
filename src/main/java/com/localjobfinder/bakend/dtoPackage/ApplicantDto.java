package com.localjobfinder.bakend.dtoPackage;

import lombok.Data;

@Data
public class ApplicantDto {

    private Long id;
    private String username;
    private String email;
    private String city;

}
