package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                                     @RequestBody BookingCreateDto bookingCreateDto) {
        log.info("POST /bookings - user: {}, itemId: {}", userId, bookingCreateDto.getItemId());
        return bookingService.create(userId, bookingCreateDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @PathVariable Long bookingId,
                                      @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{} - owner: {}, approved: {}", bookingId, userId, approved);
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @PathVariable Long bookingId) {
        log.info("GET /bookings/{} - user: {}", bookingId, userId);
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBooker(@RequestHeader(USER_ID_HEADER) Long userId,
                                                   @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings - booker: {}, state: {}", userId, state);
        return bookingService.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                                  @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings/owner - owner: {}, state: {}", userId, state);
        return bookingService.getAllByOwner(userId, state);
    }
}