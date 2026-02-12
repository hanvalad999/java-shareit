package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Доменные 404: сущности и недоступность бронирования для пользователя.
     */
    @ExceptionHandler({
            BookingNotFoundException.class,
            UserNotFoundException.class,
            ItemNotFoundException.class,
            BookingAccessDeniedException.class,
            NotFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Ошибки доступа, которые по контракту API должны возвращать 403.
     */
    @ExceptionHandler(BookingApproveForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(BookingApproveForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Доменные 400/403‑подобные ошибки (валидация и ограничения доступа владельца).
     * Здесь мы по‑прежнему используем BAD_REQUEST, чтобы не ломать существующие контракты API.
     */
    @ExceptionHandler({
            ItemNotAvailableException.class,
            OwnerBookingForbiddenException.class,
            OwnerAccessDeniedException.class,
            InvalidBookingStateException.class,
            InvalidBookingDatesException.class,
            NoCompletedBookingForCommentException.class,
            InvalidCommentException.class,
            InvalidItemDataException.class,
            BookingAlreadyProcessedException.class,
            BadRequestException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}