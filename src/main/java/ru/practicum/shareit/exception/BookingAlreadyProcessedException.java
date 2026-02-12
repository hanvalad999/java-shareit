package ru.practicum.shareit.exception;

/**
 * Попытка повторно обработать бронирование, которое уже не находится в ожидании.
 */
public class BookingAlreadyProcessedException extends BadRequestException {

    public BookingAlreadyProcessedException(String message) {
        super(message);
    }
}

