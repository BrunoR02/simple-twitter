package com.project.simple.twitter.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.project.simple.twitter.domain.Role;
import com.project.simple.twitter.repository.RoleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;

  public Role findRoleByName(String name) {
    return Optional.ofNullable(roleRepository.findByName(name))
        .orElseThrow(() -> new EntityNotFoundException(String.format("Role with name '%s' was not found", name)));
  }

  public Role getDefaultUserRole() {
    return findRoleByName("USER");
  }

}
