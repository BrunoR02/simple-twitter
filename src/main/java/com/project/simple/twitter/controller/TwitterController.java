package com.project.simple.twitter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.simple.twitter.dto.TwitterDto;
import com.project.simple.twitter.dto.request.TwitterPatchRequestDto;
import com.project.simple.twitter.dto.request.TwitterPostRequestDto;
import com.project.simple.twitter.dto.response.TwittersGetResponseDto;
import com.project.simple.twitter.service.TwitterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/twitters")
public class TwitterController {

  private final TwitterService twitterService;

  @PreAuthorize("hasAuthority('USER')")
  @PostMapping
  public ResponseEntity<Void> createTwitter(@RequestBody TwitterPostRequestDto request,
      @AuthenticationPrincipal UserDetails userDetails) {

    twitterService.create(request, userDetails);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping
  public ResponseEntity<TwittersGetResponseDto> getUserTwitters(@AuthenticationPrincipal UserDetails userDetails) {

    return new ResponseEntity<>(twitterService.getUserTwitters(userDetails), HttpStatus.CREATED);
  }

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/{id}")
  public ResponseEntity<TwitterDto> getSingleTwitter(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {

    return new ResponseEntity<>(twitterService.getSingleTwitter(id, userDetails), HttpStatus.CREATED);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateTwitter(@PathVariable Long id,
      @RequestBody TwitterPatchRequestDto request,
      @AuthenticationPrincipal UserDetails userDetails) {

    twitterService.update(id, request, userDetails);

    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @PreAuthorize("hasAuthority('USER')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> updateTwitter(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {

    twitterService.delete(id, userDetails);

    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

}
