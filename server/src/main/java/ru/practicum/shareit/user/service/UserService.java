package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {

    UserDto create(UserDto userDto);

    UserDto getById(Long id);

    User findUserEntityById(Long id);

    Collection<UserDto> getAll();

    UserDto update(Long id, UserDto userDto);

    void delete(Long id);
}