package ru.practicum.shareit.exception;

/**
 * Ошибка доступа владельца к ресурсу (вещи/бронированию).
 */
public class OwnerAccessDeniedException extends BadRequestException {

    public OwnerAccessDeniedException(String message) {
        super(message);
    }
}

