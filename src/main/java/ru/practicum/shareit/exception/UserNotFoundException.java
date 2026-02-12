package ru.practicum.shareit.exception;

/**
 * Пользователь не найден.
 */
public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }
}

