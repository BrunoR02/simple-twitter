package com.project.simple.twitter.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.project.simple.twitter.dto.twitter.TwitterDto;
import com.project.simple.twitter.dto.twitter.UpdateTwitterDto;
import com.project.simple.twitter.dto.twitter.CreateTwitterDto;
import com.project.simple.twitter.service.TwitterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/twitters")
public class TwitterController {

  private final TwitterService twitterService;

  @PostMapping
  public ResponseEntity<Void> createTwitter(@RequestBody CreateTwitterDto request,
      @AuthenticationPrincipal UserDetails userDetails) {

    twitterService.setUserDetails(userDetails);
    twitterService.create(request);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<TwitterDto>> getUserTwitters(@AuthenticationPrincipal UserDetails userDetails) {

    twitterService.setUserDetails(userDetails);

    return new ResponseEntity<>(twitterService.getUserTwitters(), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TwitterDto> getSingleTwitter(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {

    twitterService.setUserDetails(userDetails);

    return new ResponseEntity<>(twitterService.getSingleTwitter(id), HttpStatus.CREATED);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateTwitter(@PathVariable Long id,
      @RequestBody UpdateTwitterDto request,
      @AuthenticationPrincipal UserDetails userDetails) {

    twitterService.setUserDetails(userDetails);
    twitterService.update(id, request);

    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> updateTwitter(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {

    twitterService.setUserDetails(userDetails);
    twitterService.delete(id);

    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

}
