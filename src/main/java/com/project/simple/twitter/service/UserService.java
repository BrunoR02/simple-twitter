package com.project.simple.twitter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Setter
@Log4j2
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final RoleService roleService;
  private final JwtTokenService jwtTokenService;

  private UserDetails userDetails;

  public User getAuthenticatedUser() throws InvalidCredentialsException {
    if (this.userDetails == null)
      throw new InvalidCredentialsException("User is not authenticated");

    return findByUsername(this.userDetails.getUsername());
  }

  public User getAuthenticatedUser(UserDetails userDetails) throws InvalidCredentialsException {
    if (userDetails == null)
      throw new InvalidCredentialsException("User is not authenticated");

    return findByUsername(userDetails.getUsername());
  }

  private void validateCreateUserDto(CreateUserDto dto) {
    if (dto == null)
      throw new IllegalArgumentException("CreateUser object cannot be null");
    if (StringUtils.isBlank(dto.getEmail()))
      throw new IllegalArgumentException("Email cannot be null or empty");
    if (StringUtils.isBlank(dto.getPassword()))
      throw new IllegalArgumentException("Password cannot be null or empty");
    if (StringUtils.isBlank(dto.getUsername()))
      throw new IllegalArgumentException("Username cannot be null or empty");
  }

  private void validateConfirmUserDto(ConfirmUserDto dto) {
    if (dto == null)
      throw new IllegalArgumentException("ConfirmUser object cannot be null");
    if (StringUtils.isBlank(dto.getEmail()))
      throw new IllegalArgumentException("Email cannot be null or empty");
  }

  private void validateLoginUserDto(LoginUserDto dto) {
    if (dto == null)
      throw new IllegalArgumentException("LoginUser object cannot be null");
    if (StringUtils.isBlank(dto.getEmail()))
      throw new IllegalArgumentException("Email cannot be null or empty");
    if (StringUtils.isBlank(dto.getPassword()))
      throw new IllegalArgumentException("Password cannot be null or empty");
  }

  public User create(CreateUserDto dto) {
    validateCreateUserDto(dto);

    Role userRole = roleService.findRoleByName("USER");

    User newUser = User.builder()
        .username(dto.getUsername())
        .email(dto.getEmail())
        .password(SecurityConfig.passwordEncoder().encode(dto.getPassword()))
        .status(UserStatus.UNREGISTERED)
        .roles(List.of(userRole))
        .build();

    return userRepository.save(newUser);
  }

  public User findByEmail(String email) throws NotFoundException {
    return findByEmailOptional(email)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  private Optional<User> findByEmailOptional(String email) {
    return Optional.ofNullable(userRepository.findByEmail(email));
  }

  public User findByUsername(String username) throws NotFoundException {
    return findByUsernameOptional(username)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  private Optional<User> findByUsernameOptional(String username) {
    return Optional.ofNullable(userRepository.findByUsername(username));
  }

  public GenericResponseDto confirm(ConfirmUserDto dto) {
    validateConfirmUserDto(dto);

    User foundUser = findByEmail(dto.getEmail());
    LocalDateTime now = LocalDateTime.now();

    if (foundUser.isRegistered())
      return new GenericResponseDto("User is already registered");

    foundUser.setStatus(UserStatus.ACTIVE);
    foundUser.setRegisteredAt(now);
    foundUser.setUpdatedAt(now);

    userRepository.save(foundUser);

    return new GenericResponseDto("User was confirmed successfully");
  }

  public GenericResponseDto update(UpdateUserDto dto) {
    if (dto == null)
      throw new IllegalArgumentException("UpdateUser object cannot be null");

    User foundUser = getAuthenticatedUser();

    if (!foundUser.isRegistered())
      return new GenericResponseDto("User has not confirmed his account yet");

    if (dto.getDisplayName() != null)
      foundUser.setDisplayName(dto.getDisplayName());

    if (dto.getBirthDate() != null)
      foundUser.setBirthDate(dto.getBirthDate());

    foundUser.setUpdatedAt(LocalDateTime.now());

    userRepository.save(foundUser);

    return new GenericResponseDto("User was updated successfully");
  }

  public UserDto getUser() {
    User foundUser = getAuthenticatedUser();

    return UserDto.builder()
        .displayName(foundUser.getDisplayName())
        .age(foundUser.getAgeNumber())
        .createDate(foundUser.getCreatedAt().toLocalDate())
        .username(foundUser.getUsername())
        .accountStatus(foundUser.getStatus().getDisplayValue())
        .build();
  }

  public AccessUserDto login(LoginUserDto dto)
      throws InvalidCredentialsException, BadRequestException, IllegalArgumentException {
    validateLoginUserDto(dto);

    User foundUser = findByEmailOptional(dto.getEmail())
        .orElseThrow(() -> new InvalidCredentialsException("Email or password is incorrect"));

    if (!foundUser.isPasswordValid(dto.getPassword()))
      throw new InvalidCredentialsException("Email or password is incorrect");

    if (foundUser.isBlocked())
      throw new BadRequestException("User is currently blocked. Contact the support");

    CustomUserDetails userDetails = CustomUserDetails.builder()
        .username(foundUser.getUsername())
        .build();

    String accessToken = jwtTokenService.generateToken(userDetails);

    return AccessUserDto.builder()
        .accessToken(accessToken)
        .expiresIn(JwtTokenService.EXPIRATION_TIME_IN_SECONDS)
        .build();
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User foundUser = findByUsernameOptional(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    List<SimpleGrantedAuthority> authorities = foundUser.getRoles().stream()
        .map(Role::getName)
        .map(SimpleGrantedAuthority::new)
        .toList();

    return CustomUserDetails.builder()
        .authorities(authorities)
        .username(foundUser.getUsername())
        .password(foundUser.getPassword())
        .build();
  }

}
