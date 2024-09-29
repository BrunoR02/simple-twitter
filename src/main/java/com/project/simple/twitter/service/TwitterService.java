package com.project.simple.twitter.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.project.simple.twitter.domain.Twitter;
import com.project.simple.twitter.domain.User;
import com.project.simple.twitter.dto.TwitterDto;
import com.project.simple.twitter.dto.request.TwitterPatchRequestDto;
import com.project.simple.twitter.dto.request.TwitterPostRequestDto;
import com.project.simple.twitter.dto.response.TwittersGetResponseDto;
import com.project.simple.twitter.enums.TwitterVisibility;
import com.project.simple.twitter.exception.BadRequestException;
import com.project.simple.twitter.exception.InvalidArgumentException;
import com.project.simple.twitter.exception.NotFoundException;
import com.project.simple.twitter.exception.PermissionDeniedException;
import com.project.simple.twitter.repository.TwitterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TwitterService {

  private final TwitterRepository twitterRepository;
  private final UserService userService;

  public void create(TwitterPostRequestDto request, UserDetails userDetails) throws NotFoundException {
    User foundUser = userService.findByUsername(userDetails.getUsername());

    LocalDateTime now = LocalDateTime.now();

    Twitter twitter = Twitter.builder()
        .content(request.getContent())
        .author(foundUser)
        .createdAt(now)
        .updatedAt(now)
        .likes(0)
        .visibility(TwitterVisibility.PUBLIC)
        .build();

    twitterRepository.save(twitter);
  }

  private Twitter findById(Long id) throws NotFoundException {
    return twitterRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Twitter not found"));
  }

  public TwittersGetResponseDto getUserTwitters(UserDetails userDetails) {
    User foundUser = userService.findByUsername(userDetails.getUsername());

    List<Twitter> twitters = twitterRepository.findAllByAuthorId(foundUser.getId());

    List<TwitterDto> twitterDtos = twitters.stream()
        .map(TwitterDto::parse)
        .toList();

    return TwittersGetResponseDto.builder()
        .twitters(twitterDtos)
        .build();
  }

  public TwitterDto getSingleTwitter(Long id, UserDetails userDetails)
      throws NotFoundException, PermissionDeniedException {
    User foundUser = userService.findByUsername(userDetails.getUsername());

    Twitter foundTwitter = findById(id);

    if (!foundTwitter.canUserView(foundUser))
      throw new PermissionDeniedException("User does not have permission to view this twitter");

    return TwitterDto.parse(foundTwitter);
  }

  public void update(Long id, TwitterPatchRequestDto request, UserDetails userDetails)
      throws NotFoundException, BadRequestException, InvalidArgumentException {
    User foundUser = userService.findByUsername(userDetails.getUsername());

    Twitter foundTwitter = findById(id);

    if (!foundTwitter.isUserOwner(foundUser))
      throw new PermissionDeniedException("User does not have permission to update this twitter");

    if (request.getContent() != null && !request.getContent().isBlank())
      foundTwitter.setContent(request.getContent());

    if (request.getVisibilityValue() != null)
      foundTwitter.setVisibility(TwitterVisibility.parse(request.getVisibilityValue()));

    if (foundTwitter.getLikes() != request.getLikes())
      foundTwitter.setLikes(request.getLikes());

    foundTwitter.setUpdatedAt(LocalDateTime.now());

    twitterRepository.save(foundTwitter);
  }

  public void delete(Long id, UserDetails userDetails) throws NotFoundException, PermissionDeniedException {
    User foundUser = userService.findByUsername(userDetails.getUsername());

    Twitter foundTwitter = findById(id);

    if (!foundTwitter.isUserOwner(foundUser))
      throw new PermissionDeniedException("User does not have permission to delete this twitter");

    twitterRepository.deleteById(foundTwitter.getId());
  }

}
