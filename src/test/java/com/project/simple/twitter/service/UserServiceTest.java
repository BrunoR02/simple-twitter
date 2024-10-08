package com.project.simple.twitter.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.Instant;
import java.time.LocalDate;

import com.project.simple.twitter.domain.CustomUserDetails;
import com.project.simple.twitter.domain.Role;
import com.project.simple.twitter.domain.User;
import com.project.simple.twitter.dto.response.GenericResponseDto;
import com.project.simple.twitter.dto.user.AccessUserDto;
import com.project.simple.twitter.dto.user.ConfirmUserDto;
import com.project.simple.twitter.dto.user.CreateUserDto;
import com.project.simple.twitter.dto.user.LoginUserDto;
import com.project.simple.twitter.dto.user.UpdateUserDto;
import com.project.simple.twitter.dto.user.UserDto;
import com.project.simple.twitter.enums.UserStatus;
import com.project.simple.twitter.exception.BadRequestException;
import com.project.simple.twitter.exception.InvalidCredentialsException;
import com.project.simple.twitter.exception.NotFoundException;
import com.project.simple.twitter.repository.UserRepository;
import com.project.simple.twitter.security.SecurityConfig;

@ExtendWith(SpringExtension.class)
@DisplayName("UserService Test")
public class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleService roleService;

  @Mock
  private JwtTokenService jwtTokenServiceMock;

  private JwtTokenService jwtTokenService;

  @BeforeEach
  void initUtils() {
    jwtTokenService = new JwtTokenService();
  }

  private User getUserFromRepository() {
    return User.builder()
        .id(UUID.randomUUID())
        .username("test")
        .email("test@test.com")
        .displayName("Test Name")
        .birthDate(LocalDate.parse("2005-01-23"))
        .password(SecurityConfig.passwordEncoder().encode("123456"))
        .status(UserStatus.UNREGISTERED)
        .createdAt(LocalDateTime.now())
        .roles(List.of(new Role(1L, "USER")))
        .build();
  }

  @Test
  @DisplayName("getAuthenticatedUser should throw InvalidCredentialsException when userDetails is invalid")
  void getAuthenticatedUser_ShouldThrowInvalidCredentialsException_WhenUserDetailsIsInvalid() {

    // Act & Assert (when parameter UserDetails is null)
    Assertions.assertThatThrownBy(() -> userService.getAuthenticatedUser(null))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("User is not authenticated");

    userService.setUserDetails(null);

    // Act & Assert (when inside userDetails is null)
    Assertions.assertThatThrownBy(() -> userService.getAuthenticatedUser())
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("User is not authenticated");

    // Verify that the UserRepository method 'findByUsername' was not called
    verify(userRepository, never()).findByUsername(anyString());
  }

  @Test
  @DisplayName("getAuthenticatedUser should return user when userDetails is valid")
  void getAuthenticatedUser_ShouldReturnUser_WhenUserDetailsIsValid() {
    // Arrange
    User userFromRepository = getUserFromRepository();
    UserDetails userDetails = CustomUserDetails.builder()
        .username(userFromRepository.getUsername())
        .build();

    when(userRepository.findByUsername(same(userDetails.getUsername()))).thenReturn(userFromRepository);

    // Act
    User authenticatedUser = userService.getAuthenticatedUser(userDetails);

    // Assert
    Assertions.assertThat(authenticatedUser).isNotNull();

    // Verify that the UserRepository method 'findByUsername' was called correctly
    verify(userRepository, times(1)).findByUsername(same(userDetails.getUsername()));
  }

  @Test
  @DisplayName("findByEmail should throw NotFoundException when user is not found")
  void findByEmail_ShouldThrowNotFoundException_WhenUserIsNotFound() {
    // Arrange
    String searchEmail = "test@test.com";

    when(userRepository.findByEmail(same(searchEmail))).thenReturn(null);

    // Act & Assert
    Assertions.assertThatThrownBy(() -> userService.findByEmail(searchEmail))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found");

    // Verify that the UserRepository method 'findByEmail' was called correctly
    verify(userRepository, times(1)).findByEmail(same(searchEmail));
  }

  @Test
  @DisplayName("findByEmail should return user when user is found")
  void findByEmail_ShouldReturnUser_WhenUserIsFound() {
    // Arrange
    User userFromRepository = getUserFromRepository();
    String searchEmail = "test@test.com";

    when(userRepository.findByEmail(same(searchEmail))).thenReturn(userFromRepository);

    // Act
    User foundUser = userService.findByEmail(searchEmail);

    // Assert
    Assertions.assertThat(foundUser).isNotNull();
    Assertions.assertThat(foundUser.getEmail()).isEqualTo(searchEmail);

    // Verify that the UserRepository method 'findByEmail' was called correctly
    verify(userRepository, times(1)).findByEmail(same(searchEmail));
  }

  @Test
  @DisplayName("findByUsername should throw NotFoundException when user is not found")
  void findByUsername_ShouldThrowNotFoundException_WhenUserIsNotFound() {
    // Arrange
    String searchUsername = "test@test.com";

    when(userRepository.findByUsername(same(searchUsername))).thenReturn(null);

    // Act & Assert
    Assertions.assertThatThrownBy(() -> userService.findByUsername(searchUsername))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found");

    // Verify that the UserRepository method 'findByUsername' was called correctly
    verify(userRepository, times(1)).findByUsername(same(searchUsername));
  }

  @Test
  @DisplayName("findByUsername should return user when user is found")
  void findByUsername_ShouldReturnUser_WhenUserIsFound() {
    // Arrange
    User userFromRepository = getUserFromRepository();
    String searchUsername = "test@test.com";
    userFromRepository.setUsername(searchUsername);

    when(userRepository.findByUsername(same(searchUsername))).thenReturn(userFromRepository);

    // Act
    User foundUser = userService.findByUsername(searchUsername);

    // Assert
    Assertions.assertThat(foundUser).isNotNull();
    Assertions.assertThat(foundUser.getUsername()).isEqualTo(searchUsername);

    // Verify that the UserRepository method 'findByUsername' was called correctly
    verify(userRepository, times(1)).findByUsername(same(searchUsername));
  }

  @Test
  @DisplayName("create should return user when user is created successfully")
  void create_ShouldReturnUser_WhenUserIsCreatedSuccessfully() {
    // Arrange
    CreateUserDto dto = new CreateUserDto("username", "username@test.com", "password");
    String roleName = "USER";
    Role userRole = new Role(1L, roleName);

    when(roleService.findRoleByName(same(roleName))).thenReturn(userRole);
    when(userRepository.save(any(User.class))).thenAnswer((i) -> i.getArguments()[0]);

    // Act
    User createdUser = userService.create(dto);

    // Assert
    Assertions.assertThat(createdUser).isNotNull();
    Assertions.assertThat(createdUser.getUsername()).isEqualTo(dto.getUsername());
    Assertions.assertThat(createdUser.getEmail()).isEqualTo(dto.getEmail());
    Assertions.assertThat(createdUser.isPasswordValid(dto.getPassword())).isTrue();
    Assertions.assertThat(createdUser.getStatus()).isEqualTo(UserStatus.UNREGISTERED);
    Assertions.assertThat(createdUser.getRoles().stream().anyMatch(role -> role.getName().equals(userRole.getName())));

    // Verify that the RoleService method 'findRoleByName' was called correctly
    verify(roleService, times(1)).findRoleByName(same(roleName));

    // Verify that the UserRepository method 'save' was called correctly
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("create should throw IllegalArgumentException when dto is invalid")
  void create_ShouldThrowIllegalArgumentException_WhenDtoIsInvalid() {
    // Arrange
    CreateUserDto noUsernameDto = new CreateUserDto(null, "username@test.com", "password");
    CreateUserDto noEmailDto = new CreateUserDto("username", null, "password");
    CreateUserDto noPasswordDto = new CreateUserDto("username", "username@test.com", null);

    // Act & Assert (dto is null)
    Assertions.assertThatThrownBy(() -> userService.create(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("CreateUser object cannot be null");

    // Act & Assert (username is null)
    Assertions.assertThatThrownBy(() -> userService.create(noUsernameDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username cannot be null or empty");

    noUsernameDto.setUsername("");

    // Act & Assert (username is empty)
    Assertions.assertThatThrownBy(() -> userService.create(noUsernameDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username cannot be null or empty");

    // Act & Assert (email is null)
    Assertions.assertThatThrownBy(() -> userService.create(noEmailDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email cannot be null or empty");

    noEmailDto.setEmail("");

    // Act & Assert (email is empty)
    Assertions.assertThatThrownBy(() -> userService.create(noEmailDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email cannot be null or empty");

    // Act & Assert (password is null)
    Assertions.assertThatThrownBy(() -> userService.create(noPasswordDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password cannot be null or empty");

    noPasswordDto.setPassword("");

    // Act & Assert (password is empty)
    Assertions.assertThatThrownBy(() -> userService.create(noPasswordDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password cannot be null or empty");

    // Verify that the RoleService method 'findRoleByName' was not called
    verify(roleService, never()).findRoleByName(anyString());

    // Verify that the UserRepository method 'save' was not called
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("confirm should throw IllegalArgumentException when dto is invalid")
  void confirm_ShouldThrowIllegalArgumentException_WhenDtoIsInvalid() {
    // Arrange
    ConfirmUserDto noEmailDto = new ConfirmUserDto(null);

    // Act & Assert (dto is null)
    Assertions.assertThatThrownBy(() -> userService.confirm(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("ConfirmUser object cannot be null");

    // Act & Assert (email is null)
    Assertions.assertThatThrownBy(() -> userService.confirm(noEmailDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email cannot be null or empty");

    noEmailDto.setEmail("");

    // Act & Assert (email is empty)
    Assertions.assertThatThrownBy(() -> userService.confirm(noEmailDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email cannot be null or empty");

    // Verify that the UserRepository method 'findByEmail' was not called
    verify(userRepository, never()).findByEmail(anyString());

    // Verify that the UserRepository method 'save' was not called
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("confirm should return message 'User is already registered' when user already confirmed before")
  void confirm_ShouldReturnMessageAlreadyRegistered_WhenUserAlreadyConfirmedBefore() {
    // Arrange
    User userFromRepository = getUserFromRepository();
    // Set user to active that means that he is already registered
    userFromRepository.setStatus(UserStatus.ACTIVE);
    String confirmEmail = userFromRepository.getEmail();
    ConfirmUserDto confirmUserDto = new ConfirmUserDto(confirmEmail);

    when(userRepository.findByEmail(same(confirmEmail))).thenReturn(userFromRepository);

    // Act
    GenericResponseDto genericResponseDto = userService.confirm(confirmUserDto);

    // Assert
    Assertions.assertThat(genericResponseDto).isNotNull();
    Assertions.assertThat(genericResponseDto.getMessage()).isEqualTo("User is already registered");

    // Verify that the UserRepository method 'findByEmail' was called correctly
    verify(userRepository, times(1)).findByEmail(same(confirmEmail));

    // Verify that the UserRepository method 'save' was not called
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("confirm should return message 'User was confirmed successfully' when user is confirmed successfully")
  void confirm_ShouldReturnMessageConfirmedSuccessfully_WhenUserIsConfirmedSuccessfully() {
    // Arrange
    User userFromRepository = getUserFromRepository();
    // Set user to unregistered that means that he can be confirmed
    userFromRepository.setStatus(UserStatus.UNREGISTERED);
    String confirmEmail = userFromRepository.getEmail();
    ConfirmUserDto confirmUserDto = new ConfirmUserDto(confirmEmail);

    when(userRepository.findByEmail(same(confirmEmail))).thenReturn(userFromRepository);

    // Act
    GenericResponseDto genericResponseDto = userService.confirm(confirmUserDto);

    // Assert
    Assertions.assertThat(genericResponseDto).isNotNull();
    Assertions.assertThat(genericResponseDto.getMessage()).isEqualTo("User was confirmed successfully");
    Assertions.assertThat(userFromRepository.getStatus()).isEqualTo(UserStatus.ACTIVE);
    Assertions.assertThat(userFromRepository.getRegisteredAt()).isNotNull();
    Assertions.assertThat(userFromRepository.getUpdatedAt()).isNotNull();

    // Verify that the UserRepository method 'findByEmail' was not called
    verify(userRepository, times(1)).findByEmail(same(confirmEmail));

    // Verify that the UserRepository method 'save' was not called
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("update should throw IllegalArgumentException when dto is null")
  void update_ShouldThrowIllegalArgumentException_WhenDtoIsInvalid() {

    // Act & Assert (dto is null)
    Assertions.assertThatThrownBy(() -> userService.update(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("UpdateUser object cannot be null");

    // Verify that the UserRepository method 'findByEmail' was not called
    verify(userRepository, never()).findByUsername(anyString());

    // Verify that the UserRepository method 'save' was not called
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("update should return message 'User has not confirmed his account yet' when user have not confirmed yet")
  void update_ShouldReturnMessageNotConfirmedAccountYet_WhenUserHaveNotConfirmedYet() {
    // Arrange
    User userFromRepository = getUserFromRepository();
    // Set user to unregistered that means that he have not confirmed yet
    userFromRepository.setStatus(UserStatus.UNREGISTERED);
    UpdateUserDto updateUserDto = new UpdateUserDto();
    UserDetails userDetails = CustomUserDetails.builder()
        .username(userFromRepository.getUsername())
        .build();

    userService.setUserDetails(userDetails);

    when(userRepository.findByUsername(same(userDetails.getUsername()))).thenReturn(userFromRepository);

    // Act
    GenericResponseDto genericResponseDto = userService.update(updateUserDto);

    // Assert
    Assertions.assertThat(genericResponseDto).isNotNull();
    Assertions.assertThat(genericResponseDto.getMessage()).isEqualTo("User has not confirmed his account yet");

    // Verify that the UserRepository method 'findByUsername' was called correcly
    verify(userRepository, times(1)).findByUsername(same(userDetails.getUsername()));

    // Verify that the UserRepository method 'save' was not called
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("update should return message 'User was updated successfully' when user is updated successfully")
  void update_ShouldReturnMessageUpdatedSuccessfully_WhenUserIsUpdatedSuccessfully() {
    // Arrange
    User userFromRepository = getUserFromRepository();
    // Set user to unregistered that means that he is registered and can update
    userFromRepository.setStatus(UserStatus.ACTIVE);
    UpdateUserDto updateUserDto = UpdateUserDto.builder()
        .birthDate(LocalDate.parse("2002-02-07"))
        .displayName("Different Display Name")
        .build();
    UserDetails userDetails = CustomUserDetails.builder()
        .username(userFromRepository.getUsername())
        .build();

    userService.setUserDetails(userDetails);

    when(userRepository.findByUsername(same(userDetails.getUsername()))).thenReturn(userFromRepository);

    // Act
    GenericResponseDto genericResponseDto = userService.update(updateUserDto);

    // Assert
    Assertions.assertThat(genericResponseDto).isNotNull();
    Assertions.assertThat(genericResponseDto.getMessage()).isEqualTo("User was updated successfully");
    Assertions.assertThat(userFromRepository.getDisplayName()).isEqualTo(updateUserDto.getDisplayName());
    Assertions.assertThat(userFromRepository.getBirthDate()).isEqualTo(updateUserDto.getBirthDate());

    // Verify that the UserRepository method 'findByUsername' was called correcly
    verify(userRepository, times(1)).findByUsername(same(userDetails.getUsername()));

    // Verify that the UserRepository method 'save' was called correcly
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("getUser should return UserDto when successfully")
  void getUser_ShouldReturnMessageUpdatedSuccessfully_WhenUserIsUpdatedSuccessfully() {
    // Arrange
    User userFromRepository = getUserFromRepository();
    UserDetails userDetails = CustomUserDetails.builder()
        .username(userFromRepository.getUsername())
        .build();

    userService.setUserDetails(userDetails);

    when(userRepository.findByUsername(same(userDetails.getUsername()))).thenReturn(userFromRepository);

    // Act
    UserDto userDto = userService.getUser();

    // Assert
    Assertions.assertThat(userDto).isNotNull();
    Assertions.assertThat(userDto.getUsername()).isEqualTo(userFromRepository.getUsername());
    Assertions.assertThat(userDto.getDisplayName()).isEqualTo(userFromRepository.getDisplayName());
    Assertions.assertThat(userDto.getAge()).isEqualTo(userFromRepository.getAgeNumber());
    Assertions.assertThat(userDto.getCreateDate()).isEqualTo(userFromRepository.getCreatedAt().toLocalDate());
    Assertions.assertThat(userDto.getAccountStatus()).isEqualTo(userFromRepository.getStatus().getDisplayValue());

    // Verify that the UserRepository method 'findByUsername' was called correcly
    verify(userRepository, times(1)).findByUsername(same(userDetails.getUsername()));
  }

  @Test
  @DisplayName("login should throw IllegalArgumentException when dto is invalid")
  void login_ShouldThrowIllegalArgumentException_WhenDtoIsInvalid() {
    // Arrange
    LoginUserDto noEmailDto = new LoginUserDto(null, "password");
    LoginUserDto noPasswordDto = new LoginUserDto("username@test.com", null);

    // Act & Assert (dto is null)
    Assertions.assertThatThrownBy(() -> userService.login(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("LoginUser object cannot be null");

    // Act & Assert (email is null)
    Assertions.assertThatThrownBy(() -> userService.login(noEmailDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email cannot be null or empty");

    noEmailDto.setEmail("");

    // Act & Assert (email is empty)
    Assertions.assertThatThrownBy(() -> userService.login(noEmailDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email cannot be null or empty");

    // Act & Assert (password is null)
    Assertions.assertThatThrownBy(() -> userService.login(noPasswordDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password cannot be null or empty");

    noPasswordDto.setPassword("");

    // Act & Assert (password is empty)
    Assertions.assertThatThrownBy(() -> userService.login(noPasswordDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password cannot be null or empty");

    // Verify that the UserRepository method 'findByEmail' was not called
    verify(userRepository, never()).findByEmail(anyString());

    // Verify that the JwtTokenService method 'generateToken' was not called
    verify(jwtTokenServiceMock, never()).generateToken(any(UserDetails.class));
  }

  @Test
  @DisplayName("login should throw InvalidCredentialsException when no user is found by email")
  void login_ShouldThrowInvalidCredentialsException_WhenNoUserIsFoundByEmail() {
    // Arrange
    LoginUserDto dto = new LoginUserDto("username@test.com", "test");

    when(userRepository.findByEmail(same(dto.getEmail()))).thenReturn(null);

    // Act & Assert
    Assertions.assertThatThrownBy(() -> userService.login(dto))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Email or password is incorrect");

    // Verify that the UserRepository method 'findByEmail' was called correctly
    verify(userRepository, times(1)).findByEmail(same(dto.getEmail()));

    // Verify that the JwtTokenService method 'generateToken' was not called
    verify(jwtTokenServiceMock, never()).generateToken(any(UserDetails.class));
  }

  @Test
  @DisplayName("login should throw InvalidCredentialsException when password attempt is incorrect")
  void login_ShouldThrowInvalidCredentialsException_WhenPasswordAttemptIsIncorrect() {
    // Arrange
    String userPassword = "123456";
    String incorrectPasswordAttempt = "asdaild";
    LoginUserDto dto = new LoginUserDto("username@test.com", incorrectPasswordAttempt);
    User userFromRepository = getUserFromRepository();
    // Configurate password that will be different than the incorrect attempt
    userFromRepository.setPassword(SecurityConfig.passwordEncoder().encode(userPassword));

    when(userRepository.findByEmail(same(dto.getEmail()))).thenReturn(userFromRepository);

    // Act & Assert
    Assertions.assertThatThrownBy(() -> userService.login(dto))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Email or password is incorrect");

    // Verify that the UserRepository method 'findByEmail' was called correctly
    verify(userRepository, times(1)).findByEmail(same(dto.getEmail()));

    // Verify that the JwtTokenService method 'generateToken' was not called
    verify(jwtTokenServiceMock, never()).generateToken(any(UserDetails.class));
  }

  @Test
  @DisplayName("login should throw BadRequestException when user is blocked")
  void login_ShouldThrowBadRequestException_WhenUserIsBlocked() {
    // Arrange
    String userPassword = "123456";
    User userFromRepository = getUserFromRepository();
    LoginUserDto dto = new LoginUserDto("username@test.com", userPassword);
    userFromRepository.setPassword(SecurityConfig.passwordEncoder().encode(userPassword));
    userFromRepository.setStatus(UserStatus.BLOCKED);

    when(userRepository.findByEmail(same(dto.getEmail()))).thenReturn(userFromRepository);

    // Act & Assert
    Assertions.assertThatThrownBy(() -> userService.login(dto))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("User is currently blocked. Contact the support");

    // Verify that the UserRepository method 'findByEmail' was called correctly
    verify(userRepository, times(1)).findByEmail(same(dto.getEmail()));

    // Verify that the JwtTokenService method 'generateToken' was not called
    verify(jwtTokenServiceMock, never()).generateToken(any(UserDetails.class));
  }

  @Test
  @DisplayName("login should return valid access token when logged in successfully")
  void login_ShouldReturnValidAccessToken_WhenLoggedInSuccessfully() {
    // Arrange
    String userPassword = "123456";
    User userFromRepository = getUserFromRepository();
    LoginUserDto loginUserDto = new LoginUserDto("username@test.com", userPassword);
    userFromRepository.setPassword(SecurityConfig.passwordEncoder().encode(userPassword));

    UserDetails userDetails = CustomUserDetails.builder()
        .username("username")
        .build();

    Instant expiresAt = Instant.now().plus(2, ChronoUnit.HOURS);

    String accessToken = jwtTokenService.generateToken(userDetails, expiresAt);

    when(userRepository.findByEmail(same(loginUserDto.getEmail()))).thenReturn(userFromRepository);
    when(jwtTokenServiceMock.generateToken(any(UserDetails.class))).thenReturn(accessToken);

    // Act
    AccessUserDto accessUserDto = userService.login(loginUserDto);

    // Assert
    Assertions.assertThat(accessUserDto).isNotNull();
    Assertions.assertThat(accessUserDto.getAccessToken()).isNotBlank();
    Assertions.assertThat(accessUserDto.getExpiresIn()).isGreaterThan(0);
    // Check if the token is really valid by getting its subject from it and not
    // throwing any exception
    Assertions.assertThatNoException()
        .isThrownBy(() -> jwtTokenService.getTokenSubject(accessUserDto.getAccessToken()));

    // Verify that the UserRepository method 'findByEmail' was called correctly
    verify(userRepository, times(1)).findByEmail(same(loginUserDto.getEmail()));

    // Verify that the JwtTokenService method 'generateToken' was called correctly
    verify(jwtTokenServiceMock, times(1)).generateToken(any(UserDetails.class));
  }

  @Test
  @DisplayName("loadUserByUsername should throw UsernameNotFoundException when user is blocked")
  void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserIsBlocked() {
    // Arrange
    String username = "test";

    when(userRepository.findByUsername(same(username))).thenReturn(null);

    // Act & Assert
    Assertions.assertThatThrownBy(() -> userService.loadUserByUsername(username))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("User not found");

    // Verify that the UserRepository method 'findByUsername' was called correctly
    verify(userRepository, times(1)).findByUsername(same(username));
  }

  @Test
  @DisplayName("loadUserByUsername should return UserDetails when user is loaded")
  void loadUserByUsername_ShouldReturnUserDetails_WhenUserIsLoaded() {
    // Arrange
    String username = "test";
    User userFromRepository = getUserFromRepository();

    when(userRepository.findByUsername(same(username))).thenReturn(userFromRepository);

    // Act
    UserDetails loadedUserDetails = userService.loadUserByUsername(username);

    // Assert
    Assertions.assertThat(loadedUserDetails).isNotNull();
    Assertions.assertThat(loadedUserDetails.getAuthorities()).isNotNull().hasSize(userFromRepository.getRoles().size());
    Assertions.assertThat(loadedUserDetails.getUsername()).isNotBlank().isEqualTo(userFromRepository.getUsername());
    Assertions.assertThat(loadedUserDetails.getPassword()).isNotBlank().isEqualTo(userFromRepository.getPassword());

    // Verify that the UserRepository method 'findByUsername' was called correctly
    verify(userRepository, times(1)).findByUsername(same(username));
  }
}
