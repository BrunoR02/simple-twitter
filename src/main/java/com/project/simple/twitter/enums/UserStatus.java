package com.project.simple.twitter.enums;

public enum UserStatus {
  UNREGISTERED, ACTIVE, INACTIVE, BLOCKED;

  public String getDisplayValue(){
    return toString().toLowerCase();
  }
}
