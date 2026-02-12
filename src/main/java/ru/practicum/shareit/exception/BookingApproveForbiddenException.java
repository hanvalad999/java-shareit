package ru.practicum.shareit.exception;

/**
 * Попытка подтвердить или отклонить бронирование пользователем,
 * который не является владельцем вещи.
 */
public class BookingApproveForbiddenException extends RuntimeException {

    public BookingApproveForbiddenException(String message) {
        super(message);
    }
}

