package com.localjobfinder.bakend.dtoPackage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenRequiredData {

  private Long userId;
  private String AccessJwtToken;
  private String RefreshToken;

}
