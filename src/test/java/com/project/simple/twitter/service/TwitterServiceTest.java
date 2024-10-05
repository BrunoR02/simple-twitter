package com.project.simple.twitter.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.project.simple.twitter.domain.CustomUserDetails;
import com.project.simple.twitter.domain.Twitter;
import com.project.simple.twitter.domain.User;
import com.project.simple.twitter.dto.request.TwitterPostRequestDto;
import com.project.simple.twitter.enums.UserStatus;
import com.project.simple.twitter.enums.twitter.TwitterVisibility;
import com.project.simple.twitter.repository.TwitterRepository;
import com.project.simple.twitter.security.SecurityConfig;

import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
@DisplayName("TwitterService Test")
class TwitterServiceTest {

  @InjectMocks
  private TwitterService twitterService;

  @Mock
  private UserService userService;

  @Mock
  private TwitterRepository twitterRepository;

  private UserDetails userDetails;
  private User user;

  @BeforeEach
  void initUtils() {
    userDetails = CustomUserDetails.builder()
        .username("brunolucas")
        .build();

    user = User.builder()
        .username(userDetails.getUsername())
        .email("test@test.com")
        .password(SecurityConfig.passwordEncoder().encode("123456"))
        .status(UserStatus.UNREGISTERED)
        .build();
  }

  @Test
  @DisplayName("create should not throw any exception when created successfully")
  void create_ShouldNotThrowAnyException_WhenSavedSuccesfully() {
    // Arrange
    TwitterPostRequestDto request = new TwitterPostRequestDto("Test Content");
    
    when(userService.findByUsername(same(userDetails.getUsername()))).thenReturn(user);
    
    twitterService.setUserDetails(userDetails);

    // Act & Assert
    Assertions.assertThatNoException().isThrownBy(() -> twitterService.create(request));

    // Verify that the UserService method was called correctly
    verify(userService).findByUsername(same(userDetails.getUsername()));

    // Verify that the TwitterRepository method was called correctly
    verify(twitterRepository).save(any(Twitter.class));
  }

  @Test
  void testDelete() {

  }

  @Test
  void testGetSingleTwitter() {

  }

  @Test
  void testGetUserTwitters() {

  }

  @Test
  void testUpdate() {

  }
}
