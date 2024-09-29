package com.project.simple.twitter.dto.response;

import java.util.List;

import com.project.simple.twitter.dto.TwitterDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TwittersGetResponseDto {

  public List<TwitterDto> twitters;
  
}
