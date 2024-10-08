package com.project.simple.twitter.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.project.simple.twitter.domain.Twitter;
import com.project.simple.twitter.domain.User;
import com.project.simple.twitter.dto.twitter.TwitterDto;
import com.project.simple.twitter.dto.twitter.UpdateTwitterDto;
import com.project.simple.twitter.dto.twitter.CreateTwitterDto;
import com.project.simple.twitter.enums.twitter.TwitterPermission;
import com.project.simple.twitter.enums.twitter.TwitterVisibility;
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

  private User getAuthenticatedUser() throws InvalidCredentialsException {
    return userService.getAuthenticatedUser(userDetails);
  }

  private void validatePermission(Twitter twitter, User user, TwitterPermission permission)
      throws PermissionDeniedException {
    if (permission == TwitterPermission.VIEW && !twitter.canUserView(user))
      throw new PermissionDeniedException("User does not have permission to view this twitter");

    if (permission == TwitterPermission.MODIFY && !twitter.isUserOwner(user))
      throw new PermissionDeniedException("User does not have permission to modify this twitter");
  }

  private void validateCreateDto(CreateTwitterDto dto) {
    if (dto == null)
      throw new IllegalArgumentException("CreateTwitter object cannot be null");
    if (StringUtils.isEmpty(dto.getContent()))
      throw new IllegalArgumentException("Content cannot be null or empty");
  }

  public Twitter findById(Long id) throws NotFoundException {
    return twitterRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Twitter not found"));
  }

  public void create(CreateTwitterDto dto)
      throws NotFoundException, InvalidCredentialsException, IllegalArgumentException {
    validateCreateDto(dto);

    User user = getAuthenticatedUser();

    Twitter twitter = Twitter.createNew(dto.getContent(), user);

    twitterRepository.save(twitter);
  }

  public List<TwitterDto> getUserTwitters() throws InvalidCredentialsException {
    User user = getAuthenticatedUser();

    List<Twitter> twitters = twitterRepository.findAllByAuthorId(user.getId());

    return twitters.stream()
        .map(TwitterDto::parse)
        .toList();
  }

  public TwitterDto getSingleTwitter(Long id)
      throws NotFoundException, PermissionDeniedException, InvalidCredentialsException {
    User user = getAuthenticatedUser();

    Twitter foundTwitter = findById(id);

    validatePermission(foundTwitter, user, TwitterPermission.VIEW);

    return TwitterDto.parse(foundTwitter);
  }

  public Twitter update(Long id, UpdateTwitterDto dto)
      throws NotFoundException, InvalidArgumentException {
    if(dto == null)
      throw new IllegalArgumentException("UpdateTwitter object cannot be null");

    User user = getAuthenticatedUser();

    Twitter foundTwitter = findById(id);

    validatePermission(foundTwitter, user, TwitterPermission.MODIFY);

    if (StringUtils.isNotBlank(dto.getContent()))
      foundTwitter.setContent(dto.getContent());

    if (dto.getVisibilityValue() != null)
      foundTwitter.setVisibility(TwitterVisibility.parse(dto.getVisibilityValue()));

    if (foundTwitter.getLikes() != dto.getLikes())
      foundTwitter.setLikes(dto.getLikes());

    foundTwitter.setUpdatedAt(LocalDateTime.now());

    return twitterRepository.save(foundTwitter);
  }

  public void delete(Long id) throws NotFoundException, PermissionDeniedException, InvalidCredentialsException {
    User user = getAuthenticatedUser();

    Twitter foundTwitter = findById(id);

    validatePermission(foundTwitter, user, TwitterPermission.MODIFY);

    twitterRepository.deleteById(foundTwitter.getId());
  }

}
