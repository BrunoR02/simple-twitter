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

import com.project.simple.twitter.dto.response.GenericResponseDto;
import com.project.simple.twitter.dto.user.AccessUserDto;
import com.project.simple.twitter.dto.user.ConfirmUserDto;
import com.project.simple.twitter.dto.user.CreateUserDto;
import com.project.simple.twitter.dto.user.LoginUserDto;
import com.project.simple.twitter.dto.user.UpdateUserDto;
import com.project.simple.twitter.dto.user.UserDto;
import com.project.simple.twitter.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<Void> createUser(@RequestBody @Valid CreateUserDto request) {

    userService.create(request);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PatchMapping("/confirm")
  public ResponseEntity<GenericResponseDto> confirmUser(@RequestBody @Valid ConfirmUserDto request) {

    return new ResponseEntity<>(userService.confirm(request), HttpStatus.ACCEPTED);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PatchMapping
  public ResponseEntity<GenericResponseDto> updateUser(@RequestBody @Valid UpdateUserDto request,
      @AuthenticationPrincipal UserDetails userDetails) {

    userService.setUserDetails(userDetails);

    return new ResponseEntity<>(userService.update(request), HttpStatus.ACCEPTED);
  }

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/info")
  public ResponseEntity<UserDto> getUser(@AuthenticationPrincipal UserDetails userDetails) {
    
    userService.setUserDetails(userDetails);

    return new ResponseEntity<>(userService.getUser(), HttpStatus.OK);
  }

  @PostMapping("/login")
  public ResponseEntity<AccessUserDto> loginUser(@RequestBody @Valid LoginUserDto request) {

    return new ResponseEntity<>(userService.login(request), HttpStatus.OK);
  }
}
