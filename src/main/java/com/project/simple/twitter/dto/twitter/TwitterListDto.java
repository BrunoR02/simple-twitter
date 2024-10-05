package com.project.simple.twitter.dto.twitter;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TwitterListDto {

  public List<TwitterDto> twitters;
  
}
