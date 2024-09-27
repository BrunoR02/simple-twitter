package com.project.simple.twitter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.simple.twitter.dto.request.ConfirmUserPatchRequestDto;
import com.project.simple.twitter.dto.request.LoginUserPostRequestDto;
import com.project.simple.twitter.dto.request.UserPatchRequestDto;
import com.project.simple.twitter.dto.request.UserPostRequestDto;
import com.project.simple.twitter.dto.response.GenericResponseDto;
import com.project.simple.twitter.dto.response.LoginUserPostResponseDto;
import com.project.simple.twitter.dto.response.UserGetResponseDto;
import com.project.simple.twitter.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<Void> createUser(@RequestBody @Valid UserPostRequestDto request) {

    userService.create(request);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PatchMapping("/confirm")
  public ResponseEntity<GenericResponseDto> confirmUser(@RequestBody @Valid ConfirmUserPatchRequestDto request) {

    return new ResponseEntity<>(userService.confirm(request), HttpStatus.ACCEPTED);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PatchMapping
  public ResponseEntity<GenericResponseDto> updateUser(@RequestBody @Valid UserPatchRequestDto request,
      @AuthenticationPrincipal UserDetails userDetails) {

    return new ResponseEntity<>(userService.update(request, userDetails), HttpStatus.ACCEPTED);
  }

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/info")
  public ResponseEntity<UserGetResponseDto> getUser(@AuthenticationPrincipal UserDetails userDetails) {

    return new ResponseEntity<>(userService.getUserInfo(userDetails), HttpStatus.OK);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginUserPostResponseDto> loginUser(@RequestBody @Valid LoginUserPostRequestDto request) {

    return new ResponseEntity<>(userService.login(request), HttpStatus.OK);
  }
}
