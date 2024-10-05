package com.project.simple.twitter.enums.twitter;

import java.util.Optional;

import com.project.simple.twitter.exception.InvalidArgumentException;

public enum TwitterVisibility {
  PRIVATE, PUBLIC;

  public static TwitterVisibility parse(String value) throws InvalidArgumentException {
    TwitterVisibility foundItem = null;
    for (TwitterVisibility item : TwitterVisibility.values()) {
      if (item.name().toLowerCase().equals(value.toLowerCase()))
        foundItem = item;
    }

    return Optional.ofNullable(foundItem)
        .orElseThrow(() -> new InvalidArgumentException(
            "Visibility value is invalid. Only 'public' or 'private' is permitted"));
  }

}
