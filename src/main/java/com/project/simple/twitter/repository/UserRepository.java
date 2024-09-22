package com.project.simple.twitter.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.simple.twitter.domain.User;

public interface UserRepository extends JpaRepository<User,UUID> {
  
  public User findByUsername(String name);

  public User findByEmail(String email);
}
