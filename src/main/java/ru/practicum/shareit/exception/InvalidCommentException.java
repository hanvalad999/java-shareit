package ru.practicum.shareit.exception;

/**
 * Некорректные данные комментария.
 */
public class InvalidCommentException extends BadRequestException {

    public InvalidCommentException(String message) {
        super(message);
    }
}

