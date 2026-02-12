package ru.practicum.shareit.exception;

/**
 * Вещь недоступна для бронирования.
 * Доменное исключение, используемое на уровне сервисов.
 */
public class ItemNotAvailableException extends BadRequestException {

    public ItemNotAvailableException(String message) {
        super(message);
    }
}

