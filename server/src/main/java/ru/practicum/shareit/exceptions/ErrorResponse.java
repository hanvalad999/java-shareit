package ru.practicum.shareit.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    public static ErrorResponse of(int status, String error, String description) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();
    }
}