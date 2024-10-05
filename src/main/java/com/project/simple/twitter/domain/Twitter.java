package com.project.simple.twitter.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.project.simple.twitter.enums.twitter.TwitterVisibility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "twitter")
public class Twitter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String content;

  @OneToOne
  @JoinColumn(name = "author_id", referencedColumnName = "id")
  private User author;

  @Enumerated(EnumType.STRING)
  private TwitterVisibility visibility;

  private long likes;

  @CreationTimestamp
  @Column(name = "create_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public boolean isPublic() {
    return visibility == TwitterVisibility.PUBLIC;
  }

  public String getVisibilityValue() {
    return visibility.name().toLowerCase();
  }

  public boolean isEdited() {
    return !createdAt.equals(updatedAt);
  }

  public boolean canUserView(User user) {
    return this.isPublic() || isUserOwner(user);
  }

  public boolean isUserOwner(User user) {
    return user.getId().equals(this.getAuthor().getId());
  }

  public static Twitter createNew(String content, User user) {
    LocalDateTime now = LocalDateTime.now();

    return Twitter.builder()
        .content(content)
        .author(user)
        .createdAt(now)
        .updatedAt(now)
        .visibility(TwitterVisibility.PUBLIC)
        .build();
  }
}
