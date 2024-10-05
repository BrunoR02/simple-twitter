package com.project.simple.twitter.dto.twitter;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateTwitterDto {

  @NotEmpty(message = "content is required")
  private String content;

}
