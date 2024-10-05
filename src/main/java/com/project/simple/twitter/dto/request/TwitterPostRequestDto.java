package com.project.simple.twitter.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TwitterPostRequestDto {

  @NotEmpty(message = "content is required")
  private String content;

}
