package ru.practicum.shareit.exception;

/**
 * Вещь не найдена.
 */
public class ItemNotFoundException extends NotFoundException {

    public ItemNotFoundException(String message) {
        super(message);
    }
}

