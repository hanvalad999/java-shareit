package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, BookingCreateDto bookingCreateDto);

    BookingDto approve(Long userId, Long bookingId, boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getByBookerId(Long bookerId, BookingState state, int from, int size);

    List<BookingDto> getByOwnerId(Long ownerId, BookingState state, int from, int size);
}
