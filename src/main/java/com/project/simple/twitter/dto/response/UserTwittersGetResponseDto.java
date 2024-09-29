package com.project.simple.twitter.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTwittersGetResponseDto {

  public List<TwitterGetResponseDto> twitters;
  
}
