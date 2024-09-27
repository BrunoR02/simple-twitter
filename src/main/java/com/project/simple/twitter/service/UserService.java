package com.project.simple.twitter.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Period;
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
import com.project.simple.twitter.exception.InvalidCredentialsExceptions;
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
    Role defaultRole = roleService.getDefaultUserRole();

    User newUser = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(SecurityConfig.passwordEncoder().encode(request.getPassword()))
        .status(UserStatus.UNREGISTERED)
        .roles(List.of(defaultRole))
        .build();

    userRepository.save(newUser);
  }

  private User findByEmail(String email) {
    return findByEmailOptional(email)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  private Optional<User> findByEmailOptional(String email) {
    return Optional.ofNullable(userRepository.findByEmail(email));
  }

  private User findByUsername(String username) {
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

    int age = 0;
    if (foundUser.getBirthDate() != null) {
      Period agePeriod = Period.between(foundUser.getBirthDate(), LocalDate.now());
      age = agePeriod.getYears();
    }

    return UserGetResponseDto.builder()
        .displayName(foundUser.getDisplayName())
        .age(age)
        .createDate(foundUser.getCreatedAt().toLocalDate())
        .username(foundUser.getUsername())
        .accountStatus(foundUser.getStatus().getDisplayValue())
        .build();
  }

  private boolean isPasswordValid(String userPassword, String requestPassword) {
    return SecurityConfig.passwordEncoder().matches(requestPassword,userPassword);
  }

  public LoginUserPostResponseDto login(LoginUserPostRequestDto request) {
    User foundUser = findByEmailOptional(request.getEmail())
        .orElseThrow(() -> new InvalidCredentialsExceptions("Email or password is incorrect"));

    if (!isPasswordValid(foundUser.getPassword(), request.getPassword()))
      throw new InvalidCredentialsExceptions("Email or password is incorrect");

    if (foundUser.isBlocked())
      throw new BadRequestException("User is current blocked. Contact the support");

    CustomUserDetails userDetails = CustomUserDetails.builder()
        .username(foundUser.getUsername())
        .build();

    String accessToken = jwtTokenService.generateToken(userDetails);

    return LoginUserPostResponseDto.builder()
        .accessToken(accessToken)
        .expiresIn(jwtTokenService.getExpirationTime())
        .build();
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User foundUser = findByUsernameOptional(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    List<SimpleGrantedAuthority> authorities = foundUser.getRoles().stream()
        .map(Role::getName)
        .map((name) -> new SimpleGrantedAuthority(name))
        .toList();

    return CustomUserDetails.builder()
        .authorities(authorities)
        .username(foundUser.getUsername())
        .password(foundUser.getPassword())
        .build();
  }

}
