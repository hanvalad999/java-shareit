package ru.practicum.shareit.exception;

/**
 * Владелец не может бронировать собственную вещь.
 */
public class OwnerBookingForbiddenException extends BadRequestException {

    public OwnerBookingForbiddenException(String message) {
        super(message);
    }
}

