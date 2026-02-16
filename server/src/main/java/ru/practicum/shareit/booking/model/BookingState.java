package ru.practicum.shareit.booking.model;

public enum BookingState {
    ALL,        // все бронирования
    CURRENT,    // текущие бронирования
    PAST,       // завершённые бронирования
    FUTURE,     // будущие бронирования
    WAITING,    // ожидающие подтверждения
    REJECTED    // отклонённые
}