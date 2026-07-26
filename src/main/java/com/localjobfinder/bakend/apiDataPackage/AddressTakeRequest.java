package com.localjobfinder.bakend.apiDataPackage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressTakeRequest {

    @NotBlank(message = "city name is required")
    private String cityName;
}

