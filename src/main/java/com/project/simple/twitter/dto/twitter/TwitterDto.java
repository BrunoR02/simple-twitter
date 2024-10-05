package com.project.simple.twitter.dto.twitter;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.simple.twitter.domain.Twitter;
import com.project.simple.twitter.enums.twitter.TwitterVisibility;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TwitterDto {

  private long id;

  private String content;

  private String author;

  @JsonProperty(value = "created_at")
  private LocalDateTime createdAt;

  private String visibility;

  private long likes;

  private boolean edited;

  public static TwitterDto parse(Twitter twitter) {
    if (twitter == null)
      throw new IllegalArgumentException("Twitter cannot be null");
    if (twitter.getAuthor() == null)
      throw new IllegalArgumentException("Twitter author cannot be null");

    if (twitter.getCreatedAt() == null)
      twitter.setCreatedAt(LocalDateTime.now());

    if (twitter.getVisibility() == null)
      twitter.setVisibility(TwitterVisibility.PUBLIC);

    return TwitterDto.builder()
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
