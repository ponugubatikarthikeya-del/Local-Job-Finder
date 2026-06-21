package com.localjobfinder.bakend.converterPackage;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConverter {

    private String name;
    private String email;
    private String phone;

}
