package com.project.simple.twitter.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.project.simple.twitter.domain.Twitter;
import com.project.simple.twitter.domain.User;
import com.project.simple.twitter.dto.request.TwitterPatchRequestDto;
import com.project.simple.twitter.dto.request.TwitterPostRequestDto;
import com.project.simple.twitter.dto.response.TwitterGetResponseDto;
import com.project.simple.twitter.dto.response.UserTwittersGetResponseDto;
import com.project.simple.twitter.enums.twitter.TwitterPermission;
import com.project.simple.twitter.enums.twitter.TwitterVisibility;
import com.project.simple.twitter.exception.BadRequestException;
import com.project.simple.twitter.exception.InvalidArgumentException;
import com.project.simple.twitter.exception.InvalidCredentialsException;
import com.project.simple.twitter.exception.NotFoundException;
import com.project.simple.twitter.exception.PermissionDeniedException;
import com.project.simple.twitter.repository.TwitterRepository;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@RequiredArgsConstructor
@Service
public class TwitterService {

  private final TwitterRepository twitterRepository;
  private final UserService userService;

  private UserDetails userDetails;

  private User getAuthenticatedUser() {
    if (userDetails == null)
      throw new InvalidCredentialsException("User is not authenticated");

    return userService.findByUsername(userDetails.getUsername());
  }

  private void validatePermission(Twitter twitter, User user, TwitterPermission permission)
      throws PermissionDeniedException {
    if (permission == TwitterPermission.VIEW && !twitter.canUserView(user))
      throw new PermissionDeniedException("User does not have permission to view this twitter");

    if (permission == TwitterPermission.MODIFY && !twitter.isUserOwner(user))
      throw new PermissionDeniedException("User does not have permission to modify this twitter");
  }

  public void create(TwitterPostRequestDto request) throws NotFoundException, InvalidCredentialsException {
    if (request == null)
      throw new IllegalArgumentException("Request cannot be null");
    if (StringUtils.isEmpty(request.getContent()))
      throw new IllegalArgumentException("Content cannot be null or empty");

    User user = getAuthenticatedUser();

    LocalDateTime now = LocalDateTime.now();

    Twitter twitter = Twitter.builder()
        .content(request.getContent())
        .author(user)
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

  public UserTwittersGetResponseDto getUserTwitters() throws InvalidCredentialsException {
    User user = getAuthenticatedUser();

    List<Twitter> twitters = twitterRepository.findAllByAuthorId(user.getId());

    List<TwitterGetResponseDto> twitterDtos = twitters.stream()
        .map(TwitterGetResponseDto::parse)
        .toList();

    return UserTwittersGetResponseDto.builder()
        .twitters(twitterDtos)
        .build();
  }

  public TwitterGetResponseDto getSingleTwitter(Long id)
      throws NotFoundException, PermissionDeniedException, InvalidCredentialsException {
    User user = getAuthenticatedUser();

    Twitter foundTwitter = findById(id);

    validatePermission(foundTwitter, user, TwitterPermission.VIEW);

    return TwitterGetResponseDto.parse(foundTwitter);
  }

  public void update(Long id, TwitterPatchRequestDto request)
      throws NotFoundException, BadRequestException, InvalidArgumentException {
    User user = getAuthenticatedUser();

    Twitter foundTwitter = findById(id);

    validatePermission(foundTwitter, user, TwitterPermission.MODIFY);

    if (StringUtils.isNotBlank(request.getContent()))
      foundTwitter.setContent(request.getContent());

    if (request.getVisibilityValue() != null)
      foundTwitter.setVisibility(TwitterVisibility.parse(request.getVisibilityValue()));

    if (foundTwitter.getLikes() != request.getLikes())
      foundTwitter.setLikes(request.getLikes());

    foundTwitter.setUpdatedAt(LocalDateTime.now());

    twitterRepository.save(foundTwitter);
  }

  public void delete(Long id) throws NotFoundException, PermissionDeniedException, InvalidCredentialsException {
    User user = getAuthenticatedUser();

    Twitter foundTwitter = findById(id);

    validatePermission(foundTwitter, user, TwitterPermission.MODIFY);

    twitterRepository.deleteById(foundTwitter.getId());
  }

}
