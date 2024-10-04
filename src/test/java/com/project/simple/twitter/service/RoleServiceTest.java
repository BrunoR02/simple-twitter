package com.project.simple.twitter.service;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.project.simple.twitter.domain.Role;
import com.project.simple.twitter.repository.RoleRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
@DisplayName("RoleService Test")
class RoleServiceTest {

  @InjectMocks
  private RoleService roleService;

  @Mock
  private RoleRepository roleRepository;

  @Test
  @DisplayName("findRoleByName should return Role when name is valid")
  void findRoleByName_ReturnRole_WhenNameIsValid() {
    // Arrange
    String validRoleName = "USER";
    Role role = new Role(1L, validRoleName);

    when(roleRepository.findByName(same(validRoleName))).thenReturn(role);

    // Act
    Role foundRole = roleService.findRoleByName(validRoleName);

    // Assert
    Assertions.assertThat(foundRole).isNotNull();
    Assertions.assertThat(foundRole.getName()).isEqualTo(validRoleName);

    // Verify that the repository method was called correctly
    verify(roleRepository).findByName(same(validRoleName));
  }

  @Test
  @DisplayName("findRoleByName should throw EntityNotFoundException when name is invalid")
  void findRoleByName_ThrowsEntityNotFoundException_WhenNameIsInvalid() {
    // Arrange
    String invalidRoleName = "ADMIN";

    when(roleRepository.findByName(same(invalidRoleName))).thenReturn(null);

    // Act & Assert
    Assertions.assertThatThrownBy(() -> roleService.findRoleByName(invalidRoleName))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Role with name '" + invalidRoleName + "' was not found");

    // Verify that the repository method was called correctly
    verify(roleRepository).findByName(same(invalidRoleName));
  }

  @Test
  @DisplayName("findRoleByName should throw IllegalArgumentException when name is null or empty")
  void findRoleByName_ThrowsIllegalArgumentException_WhenNameIsNullOrEmpty() {

    // Act & Assert (null case)
    Assertions.assertThatThrownBy(() -> roleService.findRoleByName(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Role name cannot be null or empty");

    // Act & Assert (empty string case)
    Assertions.assertThatThrownBy(() -> roleService.findRoleByName(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Role name cannot be null or empty");

  }
}
