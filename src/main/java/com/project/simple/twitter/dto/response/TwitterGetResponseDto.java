package com.project.simple.twitter.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.simple.twitter.domain.Twitter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TwitterGetResponseDto {

  private long id;

  private String content;

  private String author;

  @JsonProperty(value = "created_at")
  private LocalDateTime createdAt;

  private String visibility;

  private long likes;

  private boolean edited;

  public static TwitterGetResponseDto parse(Twitter twitter) {
    return TwitterGetResponseDto.builder()
        .id(twitter.getId())
        .author(twitter.getAuthor().getUsername())
        .content(twitter.getContent())
        .createdAt(twitter.getCreatedAt())
        .visibility(twitter.getVisibilityValue())
        .likes(twitter.getLikes())
        .edited(twitter.isEdited())
        .build();
  }
}
