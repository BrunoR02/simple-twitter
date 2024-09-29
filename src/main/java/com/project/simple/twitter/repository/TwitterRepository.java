package com.project.simple.twitter.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.simple.twitter.domain.Twitter;

public interface TwitterRepository extends JpaRepository<Twitter, Long> {

  public List<Twitter> findAllByAuthorId(UUID authorId);
}
