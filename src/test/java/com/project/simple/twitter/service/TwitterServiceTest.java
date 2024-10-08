package com.project.simple.twitter.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.project.simple.twitter.dto.twitter.CreateTwitterDto;
import com.project.simple.twitter.dto.twitter.TwitterDto;
import com.project.simple.twitter.dto.twitter.UpdateTwitterDto;
import com.project.simple.twitter.enums.UserStatus;
import com.project.simple.twitter.enums.twitter.TwitterVisibility;
import com.project.simple.twitter.exception.InvalidArgumentException;
import com.project.simple.twitter.exception.NotFoundException;
import com.project.simple.twitter.exception.PermissionDeniedException;
import com.project.simple.twitter.repository.TwitterRepository;
import com.project.simple.twitter.security.SecurityConfig;

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
  private User anotherUser;

  @BeforeEach
  void initUtils() {

    userDetails = CustomUserDetails.builder()
        .username("brunolucas")
        .build();

    twitterService.setUserDetails(userDetails);

    user = User.builder()
        .id(UUID.randomUUID())
        .username(userDetails.getUsername())
        .email("user@test.com")
        .password(SecurityConfig.passwordEncoder().encode("123456"))
        .status(UserStatus.ACTIVE)
        .build();

    anotherUser = User.builder()
        .id(UUID.randomUUID())
        .username(userDetails.getUsername())
        .email("anotheruser@test.com")
        .password(SecurityConfig.passwordEncoder().encode("123456"))
        .status(UserStatus.ACTIVE)
        .build();
  }

  private Twitter getTwitterFromRepository() {
    LocalDateTime now = LocalDateTime.now();

    return Twitter.builder()
        .id(1L)
        .content("Test Content")
        .author(user)
        .createdAt(now)
        .updatedAt(now)
        .visibility(TwitterVisibility.PUBLIC)
        .build();
  }

  private Twitter getTwitterCopy(Twitter twitter) {
    return Twitter.builder()
        .id(twitter.getId() + 1L)
        .content(twitter.getContent())
        .author(twitter.getAuthor())
        .createdAt(twitter.getCreatedAt())
        .updatedAt(twitter.getUpdatedAt())
        .visibility(twitter.getVisibility())
        .build();
  }

  @Test
  @DisplayName("findById should throw NotFoundException when twitter is not found")
  void findById_ShouldThrowNotFoundException_WhenTwitterIsNotFound() {
    // Arrange
    Long searchId = 1L;

    when(twitterRepository.findById(same(searchId))).thenReturn(Optional.ofNullable(null));

    // Act & Assert
    Assertions.assertThatThrownBy(() -> twitterService.findById(searchId))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Twitter not found");

    // Verify that TwitterRepository method 'findById' was called correctly
    verify(twitterRepository, times(1)).findById(same(searchId));
  }

  @Test
  @DisplayName("findById should return twitter when twitter is found")
  void findById_ShouldReturnTwitter_WhenTwitterIsFound() {
    // Arrange
    Twitter twitterFromRepository = getTwitterFromRepository();
    Long searchId = twitterFromRepository.getId();

    when(twitterRepository.findById(same(searchId))).thenReturn(Optional.ofNullable(twitterFromRepository));

    // Act
    Twitter foundTwitter = twitterService.findById(searchId);

    // Assert
    Assertions.assertThat(foundTwitter).isNotNull();
    Assertions.assertThat(foundTwitter.getId()).isEqualTo(twitterFromRepository.getId());

    // Verify that TwitterRepository method 'findById' was called correctly
    verify(twitterRepository, times(1)).findById(same(searchId));
  }

  @Test
  @DisplayName("create should not throw any exception when successfully")
  void create_ShouldNotThrowAnyException_WhenCreatedSuccesfully() {
    // Arrange
    CreateTwitterDto dto = new CreateTwitterDto("Test Content");

    when(userService.getAuthenticatedUser(same(userDetails))).thenReturn(user);

    // Act & Assert
    Assertions.assertThatNoException().isThrownBy(() -> twitterService.create(dto));

    // Verify that the UserService method 'getAuthenticatedUser' was called
    // correctly
    verify(userService, times(1)).getAuthenticatedUser(same(userDetails));

    // Verify that the TwitterRepository method 'save' was called correctly
    verify(twitterRepository, times(1)).save(any(Twitter.class));
  }

  @Test
  @DisplayName("create should throw IllegalArgumentException when dto is invalid")
  void create_ShouldThrowIllegalArgumentException_WhenDtoIsInvalid() {
    // Arrange
    CreateTwitterDto invalidContentDto = new CreateTwitterDto("");

    // Act & Assert (Dto is null)
    Assertions.assertThatThrownBy(() -> twitterService.create(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("CreateTwitter object cannot be null");

    // Act & Assert (Content is null or empty)
    Assertions.assertThatThrownBy(() -> twitterService.create(invalidContentDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Content cannot be null or empty");

    // Verify that the UserService method 'getAuthenticatedUser' was not called
    verify(userService, never()).getAuthenticatedUser(same(userDetails));

    // Verify that the TwitterRepository method 'save' was not called
    verify(twitterRepository, never()).save(any(Twitter.class));
  }

  @Test
  @DisplayName("getUserTwitters should not return empty twitter list when twitters are found")
  void getUserTwitters_ShouldNotReturnEmptyTwitterList_WhenTwittersAreFound() {
    // Arrange
    List<Twitter> twitters = List.of(
        Twitter.createNew("Twitter Content 1", user),
        Twitter.createNew("Twitter Content 2", user),
        Twitter.createNew("Twitter Content 3", user));

    when(userService.getAuthenticatedUser(same(userDetails))).thenReturn(user);
    when(twitterRepository.findAllByAuthorId(same(user.getId()))).thenReturn(twitters);

    // Act
    List<TwitterDto> userTwitters = twitterService.getUserTwitters();

    // Assert
    Assertions.assertThat(userTwitters)
        .isNotNull()
        .isNotEmpty()
        .hasSize(twitters.size());

    // Verify that the UserService method 'getAuthenticatedUser' was called correcly
    verify(userService, times(1)).getAuthenticatedUser(same(userDetails));

    // Verify that TwitterRepository method 'findAllByAuthorId' was called correctly
    verify(twitterRepository, times(1)).findAllByAuthorId(same(user.getId()));
  }

  @Test
  @DisplayName("getUserTwitters should return empty twitter list when no twitters are found")
  void getUserTwitters_ShouldReturnEmptyTwitterList_WhenNoTwittersAreFound() {

    when(userService.getAuthenticatedUser(same(userDetails))).thenReturn(user);
    when(twitterRepository.findAllByAuthorId(same(user.getId()))).thenReturn(List.of());

    // Act
    List<TwitterDto> userTwitters = twitterService.getUserTwitters();

    // Assert
    Assertions.assertThat(userTwitters)
        .isNotNull()
        .isEmpty();

    // Verify that the UserService method 'getAuthenticatedUser' was called correcly
    verify(userService, times(1)).getAuthenticatedUser(same(userDetails));

    // Verify that TwitterRepository method 'findAllByAuthorId' was called correctly
    verify(twitterRepository, times(1)).findAllByAuthorId(same(user.getId()));
  }

  @Test
  @DisplayName("getSingleTwitter should return twitter when twitter is found and user have permission to view")
  void getSingleTwitter_ShouldReturnTwitter_WhenTwitterIsFoundAndUserHavePermissionToView() {
    // Arrange
    Twitter publicTwitterFromRepository = getTwitterFromRepository();
    publicTwitterFromRepository.setVisibility(TwitterVisibility.PUBLIC);
    Long searchId = publicTwitterFromRepository.getId();

    when(userService.getAuthenticatedUser(same(userDetails))).thenReturn(user);
    when(twitterRepository.findById(same(searchId))).thenReturn(Optional.ofNullable(publicTwitterFromRepository));

    // Act
    TwitterDto singleTwitter = twitterService.getSingleTwitter(searchId);

    // Assert
    Assertions.assertThat(singleTwitter).isNotNull();
    Assertions.assertThat(singleTwitter.getId()).isEqualTo(publicTwitterFromRepository.getId());
    Assertions.assertThat(singleTwitter.getContent()).isEqualTo(publicTwitterFromRepository.getContent());

    // Verify that the UserService method 'getAuthenticatedUser' was called correcly
    verify(userService, times(1)).getAuthenticatedUser(same(userDetails));

    // Verify that TwitterRepository method 'findById' was called correctly
    verify(twitterRepository, times(1)).findById(same(searchId));
  }

  @Test
  @DisplayName("getSingleTwitter should throw PermissionDeniedException when twitter is found but user does not have permission to view")
  void getSingleTwitter_ShouldThrowPermissionDeniedException_WhenTwitterIsFoundButUserDoesNotHavePermissionToView() {
    // Arrange
    Twitter privateTwitterFromRepository = getTwitterFromRepository();
    privateTwitterFromRepository.setAuthor(user);
    privateTwitterFromRepository.setVisibility(TwitterVisibility.PRIVATE);
    Long searchId = privateTwitterFromRepository.getId();

    // Authentication done with different user than the private twitter subject
    when(userService.getAuthenticatedUser(same(userDetails))).thenReturn(anotherUser);
    when(twitterRepository.findById(same(searchId))).thenReturn(Optional.ofNullable(privateTwitterFromRepository));

    // Act & Assert
    Assertions.assertThatThrownBy(() -> twitterService.getSingleTwitter(searchId))
        .isInstanceOf(PermissionDeniedException.class)
        .hasMessage("User does not have permission to view this twitter");

    // Verify that the UserService method 'getAuthenticatedUser' was called correcly
    verify(userService, times(1)).getAuthenticatedUser(same(userDetails));

    // Verify that TwitterRepository method 'findById' was called correctly
    verify(twitterRepository, times(1)).findById(same(searchId));
  }

  @Test
  @DisplayName("update should throw IllegalArgumentException when dto is null")
  void update_ShouldThrowIllegalArgumentException_WhenDtoIsNull() {

    // Act & Assert
    Assertions.assertThatThrownBy(() -> twitterService.update(1L, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("UpdateTwitter object cannot be null");

    // Verify that the UserService method 'getAuthenticatedUser' was not called
    verify(userService, never()).getAuthenticatedUser(same(userDetails));

    // Verify that TwitterRepository method 'findById' was not called
    verify(twitterRepository, never()).findById(anyLong());

    // Verify that the TwitterRepository method 'save' was not called
    verify(twitterRepository, never()).save(any(Twitter.class));
  }

  @Test
  @DisplayName("update should throw PermissionDeniedException when twitter is found but user does not have permission to modify")
  void update_ShouldThrowPermissionDeniedException_WhenTwitterIsFoundButUserDoesNotHavePermissionToModify() {
    // Arrange
    UpdateTwitterDto dto = new UpdateTwitterDto();
    Twitter twitterFromRepository = getTwitterFromRepository();
    twitterFromRepository.setAuthor(user);
    Long searchId = twitterFromRepository.getId();

    // Authentication done with different user than the twitter subject
    when(userService.getAuthenticatedUser(same(userDetails))).thenReturn(anotherUser);
    when(twitterRepository.findById(same(searchId))).thenReturn(Optional.ofNullable(twitterFromRepository));

    // Act & Assert
    Assertions.assertThatThrownBy(() -> twitterService.update(searchId, dto))
        .isInstanceOf(PermissionDeniedException.class)
        .hasMessage("User does not have permission to modify this twitter");

    // Verify that the UserService method 'getAuthenticatedUser' was called correcly
    verify(userService, times(1)).getAuthenticatedUser(same(userDetails));

    // Verify that TwitterRepository method 'findById' was called correctly
    verify(twitterRepository, times(1)).findById(same(searchId));

    // Verify that the TwitterRepository method 'save' was not called
    verify(twitterRepository, never()).save(any(Twitter.class));
  }

  @Test
  @DisplayName("update should return updated twitter when twitter is found and user have permission to modify")
  void update_ShouldNotThrowAnyException_WhenTwitterIsFoundAndUserHavePermissionToModify() {
    // Arrange
    UpdateTwitterDto dto = UpdateTwitterDto.builder()
        .content("Different Test Content")
        .visibilityValue("public")
        .likes(123)
        .build();
    Twitter twitterFromRepository = getTwitterFromRepository();
    twitterFromRepository.setContent("Test Content");
    twitterFromRepository.setVisibility(TwitterVisibility.PRIVATE);
    twitterFromRepository.setLikes(50);
    twitterFromRepository.setAuthor(user);
    Long searchId = twitterFromRepository.getId();

    // Authentication done with same user of the twitter subject
    when(userService.getAuthenticatedUser(same(userDetails))).thenReturn(user);
    // Mock with return a copy of the defined twitterFromRepository to get a
    // different instance when modified inside the 'update' method
    when(twitterRepository.findById(same(searchId)))
        .thenReturn(Optional.ofNullable(getTwitterCopy(twitterFromRepository)));

    when(twitterRepository.save(any(Twitter.class)))
        .thenAnswer((invocation) -> invocation.getArguments()[0]);

    // Act
    Twitter updatedTwitter = twitterService.update(searchId, dto);

    // Assert
    Assertions.assertThat(updatedTwitter.getContent()).isNotEqualTo(twitterFromRepository.getContent());
    Assertions.assertThat(updatedTwitter.getVisibility()).isNotEqualTo(twitterFromRepository.getVisibility());
    Assertions.assertThat(updatedTwitter.getLikes()).isNotEqualTo(twitterFromRepository.getLikes());

    // Verify that the UserService method 'getAuthenticatedUser' was called correcly
    verify(userService, times(1)).getAuthenticatedUser(same(userDetails));

    // Verify that TwitterRepository method 'findById' was called correctly
    verify(twitterRepository, times(1)).findById(same(searchId));

    // Verify that the TwitterRepository method 'save' was called correctly
    verify(twitterRepository, times(1)).save(any(Twitter.class));
  }

  @Test
  @DisplayName("update should throw InvalidArgumentException when twitter is found but the new visibility value is invalid")
  void update_ShouldThrowInvalidArgumentException_WhenTwitterIsFoundButNewVisibilityValueIsInvalid() {
    // Arrange
    String invalidVisibilityValue = "ashusdas";
    UpdateTwitterDto dto = UpdateTwitterDto.builder()
        .visibilityValue(invalidVisibilityValue)
        .build();
    Twitter twitterFromRepository = getTwitterFromRepository();
    Long searchId = twitterFromRepository.getId();

    when(userService.getAuthenticatedUser(same(userDetails))).thenReturn(user);
    when(twitterRepository.findById(same(searchId))).thenReturn(Optional.ofNullable(twitterFromRepository));

    // Act & Assert
    Assertions.assertThatThrownBy(() -> twitterService.update(searchId, dto))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessage("Visibility value is invalid. Only 'public' or 'private' is permitted");

    // Verify that the UserService method 'getAuthenticatedUser' was called correcly
    verify(userService, times(1)).getAuthenticatedUser(same(userDetails));

    // Verify that TwitterRepository method 'findById' was called correctly
    verify(twitterRepository, times(1)).findById(same(searchId));

    // Verify that the TwitterRepository method 'save' was not called
    verify(twitterRepository, never()).save(any(Twitter.class));
  }

  @Test
  @DisplayName("delete should throw PermissionDeniedException when twitter is found but user does not have permission to modify")
  void delete_ShouldThrowPermissionDeniedException_WhenTwitterIsFoundButUserDoesNotHavePermissionToModify() {
    // Arrange
    Twitter validTwitter = getTwitterFromRepository();
    validTwitter.setAuthor(user);
    Long deleteId = validTwitter.getId();

    // Authentication done with different user than the twitter subject
    when(userService.getAuthenticatedUser(same(userDetails))).thenReturn(anotherUser);
    when(twitterRepository.findById(same(deleteId))).thenReturn(Optional.ofNullable(validTwitter));

    // Act & Assert
    Assertions.assertThatThrownBy(() -> twitterService.delete(deleteId))
        .isInstanceOf(PermissionDeniedException.class)
        .hasMessage("User does not have permission to modify this twitter");

    // Verify that the UserService method 'getAuthenticatedUser' was called correcly
    verify(userService, times(1)).getAuthenticatedUser(same(userDetails));

    // Verify that TwitterRepository method 'findById' was called correctly
    verify(twitterRepository, times(1)).findById(same(deleteId));

    // Verify that the TwitterRepository method 'deleteById' was not called
    verify(twitterRepository, never()).deleteById(same(deleteId));
  }

  @Test
  @DisplayName("delete should not throw any exception when twitter is found and user have permission to modify")
  void delete_ShouldNotThrowAnyException_WhenTwitterIsFoundAndUserHavePermissionToModify() {
    // Arrange
    Twitter validTwitter = getTwitterFromRepository();
    validTwitter.setAuthor(user);
    Long deleteId = validTwitter.getId();

    twitterService.setUserDetails(userDetails);

    // Authentication done with same user of the twitter subject
    when(userService.getAuthenticatedUser(same(userDetails))).thenReturn(user);
    when(twitterRepository.findById(same(deleteId))).thenReturn(Optional.ofNullable(validTwitter));

    // Act & Assert
    Assertions.assertThatNoException().isThrownBy(() -> twitterService.delete(deleteId));

    // Verify that the UserService method 'getAuthenticatedUser' was called correcly
    verify(userService, times(1)).getAuthenticatedUser(same(userDetails));

    // Verify that TwitterRepository method 'findById' was called correctly
    verify(twitterRepository, times(1)).findById(same(deleteId));

    // Verify that the TwitterRepository method 'deleteById' was called correctly
    verify(twitterRepository, times(1)).deleteById(same(deleteId));
  }
}
