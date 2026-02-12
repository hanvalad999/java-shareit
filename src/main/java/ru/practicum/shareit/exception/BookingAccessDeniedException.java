package ru.practicum.shareit.exception;

/**
 * Бронирование недоступно пользователю (он не владелец и не бронирующий).
 */
public class BookingAccessDeniedException extends NotFoundException {

    public BookingAccessDeniedException(String message) {
        super(message);
    }
}

