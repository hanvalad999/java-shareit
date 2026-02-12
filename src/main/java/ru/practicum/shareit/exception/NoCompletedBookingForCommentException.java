package ru.practicum.shareit.exception;

/**
 * Пользователь не имеет завершённых бронирований вещи для публикации комментария.
 */
public class NoCompletedBookingForCommentException extends BadRequestException {

    public NoCompletedBookingForCommentException(String message) {
        super(message);
    }
}

