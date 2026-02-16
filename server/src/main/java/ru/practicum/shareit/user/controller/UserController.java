package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("POST /users - Creating user: {}", userDto.getEmail());
        return userService.create(userDto);
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        log.info("GET /users/{} - Getting user by id", id);
        return userService.getById(id);
    }

    @GetMapping
    public Collection<UserDto> getAll() {
        log.info("GET /users - Getting all users");
        return userService.getAll();
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto userDto) {
        log.info("PATCH /users/{} - Updating user", id);
        return userService.update(id, userDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("DELETE /users/{} - Deleting user", id);
        userService.delete(id);
    }
}