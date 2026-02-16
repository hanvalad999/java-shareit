package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto create(Long bookerId, BookingCreateDto bookingCreateDto);

    BookingResponseDto approve(Long ownerId, Long bookingId, Boolean approved);

    BookingResponseDto getById(Long userId, Long bookingId);

    List<BookingResponseDto> getAllByBooker(Long bookerId, BookingState state);

    List<BookingResponseDto> getAllByOwner(Long ownerId, BookingState state);
}