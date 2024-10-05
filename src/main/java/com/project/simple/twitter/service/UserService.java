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
import com.project.simple.twitter.dto.request.ConfirmUserPatchRequestDto;
import com.project.simple.twitter.dto.request.LoginUserPostRequestDto;
import com.project.simple.twitter.dto.request.UserPatchRequestDto;
import com.project.simple.twitter.dto.request.UserPostRequestDto;
import com.project.simple.twitter.dto.response.GenericResponseDto;
import com.project.simple.twitter.dto.response.LoginUserPostResponseDto;
import com.project.simple.twitter.dto.response.UserGetResponseDto;
import com.project.simple.twitter.enums.UserStatus;
import com.project.simple.twitter.exception.BadRequestException;
import com.project.simple.twitter.exception.InvalidCredentialsException;
import com.project.simple.twitter.exception.NotFoundException;
import com.project.simple.twitter.repository.UserRepository;
import com.project.simple.twitter.security.SecurityConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final RoleService roleService;
  private final JwtTokenService jwtTokenService;

  public void create(UserPostRequestDto request) {
    Role userRole = roleService.findRoleByName("USER");

    User newUser = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(SecurityConfig.passwordEncoder().encode(request.getPassword()))
        .status(UserStatus.UNREGISTERED)
        .roles(List.of(userRole))
        .build();

    userRepository.save(newUser);
  }

  private User findByEmail(String email) throws NotFoundException {
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

  public GenericResponseDto confirm(ConfirmUserPatchRequestDto request) {
    User foundUser = findByEmail(request.getEmail());
    LocalDateTime now = LocalDateTime.now();

    if (foundUser.isRegistered())
      return new GenericResponseDto("User is already registered");

    foundUser.setStatus(UserStatus.ACTIVE);
    foundUser.setRegisteredAt(now);
    foundUser.setUpdatedAt(now);

    userRepository.save(foundUser);

    return new GenericResponseDto("User was confirmed successfully");
  }

  public GenericResponseDto update(UserPatchRequestDto request, UserDetails userDetails) {
    User foundUser = findByUsername(userDetails.getUsername());

    if (!foundUser.isRegistered())
      return new GenericResponseDto("User has not confirmed his account yet");

    if (request.getDisplayName() != null)
      foundUser.setDisplayName(request.getDisplayName());

    if (request.getBirthDate() != null)
      foundUser.setBirthDate(request.getBirthDate());

    foundUser.setUpdatedAt(LocalDateTime.now());

    userRepository.save(foundUser);

    return new GenericResponseDto("User was updated successfully");
  }

  public UserGetResponseDto getUserInfo(UserDetails userDetails) {
    User foundUser = findByUsername(userDetails.getUsername());

    return UserGetResponseDto.builder()
        .displayName(foundUser.getDisplayName())
        .age(foundUser.getAgeNumber())
        .createDate(foundUser.getCreatedAt().toLocalDate())
        .username(foundUser.getUsername())
        .accountStatus(foundUser.getStatus().getDisplayValue())
        .build();
  }

  public LoginUserPostResponseDto login(LoginUserPostRequestDto request)
      throws InvalidCredentialsException, BadRequestException {
    User foundUser = findByEmailOptional(request.getEmail())
        .orElseThrow(() -> new InvalidCredentialsException("Email or password is incorrect"));

    if (!foundUser.isPasswordValid(request.getPassword()))
      throw new InvalidCredentialsException("Email or password is incorrect");

    if (foundUser.isBlocked())
      throw new BadRequestException("User is currently blocked. Contact the support");

    CustomUserDetails userDetails = CustomUserDetails.builder()
        .username(foundUser.getUsername())
        .build();

    String accessToken = jwtTokenService.generateToken(userDetails);

    return LoginUserPostResponseDto.builder()
        .accessToken(accessToken)
        .expiresIn(jwtTokenService.getExpirationTimeInSeconds())
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
