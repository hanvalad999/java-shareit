package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void create_shouldCreateUser_whenValidData() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        UserDto created = userService.create(userDto);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("John Doe");
        assertThat(created.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void create_shouldThrowConflictException_whenEmailAlreadyExists() {
        UserDto userDto1 = UserDto.builder()
                .name("John Doe")
                .email("duplicate@example.com")
                .build();
        userService.create(userDto1);

        UserDto userDto2 = UserDto.builder()
                .name("Jane Doe")
                .email("duplicate@example.com")
                .build();

        assertThatThrownBy(() -> userService.create(userDto2))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void getById_shouldReturnUser_whenUserExists() {
        UserDto created = userService.create(UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build());

        UserDto found = userService.getById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("John Doe");
        assertThat(found.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getById_shouldThrowNotFoundException_whenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        userService.create(UserDto.builder()
                .name("User 1")
                .email("user1@example.com")
                .build());
        userService.create(UserDto.builder()
                .name("User 2")
                .email("user2@example.com")
                .build());

        Collection<UserDto> users = userService.getAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserDto::getName)
                .containsExactlyInAnyOrder("User 1", "User 2");
    }

    @Test
    void update_shouldUpdateName_whenNameProvided() {
        UserDto created = userService.create(UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build());

        UserDto updateDto = UserDto.builder()
                .name("John Updated")
                .build();

        UserDto updated = userService.update(created.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("John Updated");
        assertThat(updated.getEmail()).isEqualTo("john@example.com"); // Email unchanged
    }

    @Test
    void update_shouldUpdateEmail_whenEmailIsUnique() {
        UserDto created = userService.create(UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build());

        UserDto updateDto = UserDto.builder()
                .email("john.new@example.com")
                .build();

        UserDto updated = userService.update(created.getId(), updateDto);

        assertThat(updated.getEmail()).isEqualTo("john.new@example.com");
        assertThat(updated.getName()).isEqualTo("John Doe"); // Name unchanged
    }

    @Test
    void update_shouldThrowConflictException_whenEmailAlreadyTaken() {
        userService.create(UserDto.builder()
                .name("User 1")
                .email("user1@example.com")
                .build());
        UserDto user2 = userService.create(UserDto.builder()
                .name("User 2")
                .email("user2@example.com")
                .build());

        UserDto updateDto = UserDto.builder()
                .email("user1@example.com") // Try to use User 1's email
                .build();

        assertThatThrownBy(() -> userService.update(user2.getId(), updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void update_shouldThrowNotFoundException_whenUserDoesNotExist() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .build();

        assertThatThrownBy(() -> userService.update(999L, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void delete_shouldDeleteUser_whenUserExists() {
        UserDto created = userService.create(UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build());

        userService.delete(created.getId());

        assertThatThrownBy(() -> userService.getById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }
}