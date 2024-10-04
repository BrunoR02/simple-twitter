package com.project.simple.twitter.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.project.simple.twitter.domain.Role;
import com.project.simple.twitter.domain.User;
import com.project.simple.twitter.enums.UserStatus;
import com.project.simple.twitter.security.SecurityConfig;

import java.util.List;

@DataJpaTest
@DisplayName("Tests for User Repository")
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Test
  void save_PersistUser_WhenSuccessful(){
    User newUser = getUser();
    
    userRepository.save(newUser);

    User savedUser = userRepository.findByEmail(newUser.getEmail());

    Assertions.assertThat(savedUser).isNotNull();
    Assertions.assertThat(savedUser.getId()).isNotNull();
    Assertions.assertThat(savedUser.getUsername()).isEqualTo(newUser.getUsername());
    Assertions.assertThat(savedUser.getEmail()).isEqualTo(newUser.getEmail());
    Assertions.assertThat(savedUser.getPassword()).isEqualTo(newUser.getPassword());
    Assertions.assertThat(savedUser.getStatus()).isEqualTo(newUser.getStatus());
  }

  private User getUser() {
    return User.builder()
        .username("brunolucas")
        .email("brunolucas23@gmail.com")
        .password(SecurityConfig.passwordEncoder().encode("bruno123"))
        .status(UserStatus.UNREGISTERED)
        .roles(List.of(saveNewRole()))
        .build();
  }

  private Role saveNewRole(){
    Role role = new Role(null,"USER");

    return roleRepository.save(role);
  }

}
