package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        validateForCreate(userDto);
        validateEmailFormat(userDto.getEmail());
        ensureEmailUnique(userDto.getEmail(), null);
        User savedUser = userRepository.save(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (StringUtils.hasText(userDto.getName())) {
            existing.setName(userDto.getName());
        }
        if (StringUtils.hasText(userDto.getEmail())) {
            validateEmailFormat(userDto.getEmail());
            ensureEmailUnique(userDto.getEmail(), id);
            existing.setEmail(userDto.getEmail());
        }

        userRepository.save(existing);
        return UserMapper.toUserDto(existing);
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private void ensureEmailUnique(String email, Long userId) {
        if (email != null && userRepository.existsByEmailIgnoreUser(email, userId)) {
            throw new ConflictException("Email already used");
        }
    }

    private void validateForCreate(UserDto userDto) {
        if (userDto == null || !StringUtils.hasText(userDto.getName()) || !StringUtils.hasText(userDto.getEmail())) {
            throw new BadRequestException("Name and email are required");
        }
    }

    private void validateEmailFormat(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            throw new BadRequestException("Invalid email");
        }
    }
}
