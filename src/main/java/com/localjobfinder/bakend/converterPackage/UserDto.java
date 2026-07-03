package com.localjobfinder.bakend.converterPackage;

import lombok.Data;

import java.time.Instant;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;
}
