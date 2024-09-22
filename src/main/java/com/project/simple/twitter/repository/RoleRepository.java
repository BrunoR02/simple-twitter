package com.project.simple.twitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.simple.twitter.domain.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

  public Role findByName(String name);
}
