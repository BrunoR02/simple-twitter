package com.project.simple.twitter.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.simple.twitter.domain.Role;
import com.project.simple.twitter.domain.User;
import com.project.simple.twitter.dto.request.ConfirmUserPatchRequestDto;
import com.project.simple.twitter.dto.request.UserPatchRequestDto;
import com.project.simple.twitter.dto.request.UserPostRequestDto;
import com.project.simple.twitter.dto.response.GenericResponseDto;
import com.project.simple.twitter.dto.response.UserGetResponseDto;
import com.project.simple.twitter.enums.UserStatus;
import com.project.simple.twitter.exception.NotFoundException;
import com.project.simple.twitter.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleService roleService;
  private final PasswordEncoder passwordEncoder;

  public void create(UserPostRequestDto request) {
    Role defaultRole = roleService.getDefaultUserRole();

    User newUser = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .status(UserStatus.UNREGISTERED)
        .roles(List.of(defaultRole))
        .build();

    userRepository.save(newUser);
  }

  private User findByEmail(String email) {
    return Optional.ofNullable(userRepository.findByEmail(email))
        .orElseThrow(() -> new NotFoundException("User not found"));
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

  public GenericResponseDto update(UserPatchRequestDto request) {
    User foundUser = findByEmail(request.getEmail());

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

  public UserGetResponseDto getSingleUser(String email) {
    User foundUser = findByEmail(email);

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

}
