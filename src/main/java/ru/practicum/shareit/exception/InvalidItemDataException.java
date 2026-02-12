package ru.practicum.shareit.exception;

/**
 * Некорректные данные вещи при создании/обновлении.
 */
public class InvalidItemDataException extends BadRequestException {

    public InvalidItemDataException(String message) {
        super(message);
    }
}

