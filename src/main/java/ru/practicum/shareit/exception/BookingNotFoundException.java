package ru.practicum.shareit.exception;

/**
 * Бронирование не найдено.
 */
public class BookingNotFoundException extends NotFoundException {

    public BookingNotFoundException(String message) {
        super(message);
    }
}

