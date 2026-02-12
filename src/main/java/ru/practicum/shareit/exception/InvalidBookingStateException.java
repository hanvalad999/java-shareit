package ru.practicum.shareit.exception;

/**
 * Некорректное значение фильтра состояния бронирования.
 */
public class InvalidBookingStateException extends BadRequestException {

    public InvalidBookingStateException(String message) {
        super(message);
    }
}

