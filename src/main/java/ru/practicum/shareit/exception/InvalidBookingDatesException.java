package ru.practicum.shareit.exception;

/**
 * Некорректные даты бронирования.
 */
public class InvalidBookingDatesException extends BadRequestException {

    public InvalidBookingDatesException(String message) {
        super(message);
    }
}

