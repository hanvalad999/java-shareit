package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto create(Long bookerId, BookingCreateDto bookingCreateDto) {
        // Validate dates
        if (bookingCreateDto.getEnd().isBefore(bookingCreateDto.getStart()) ||
                bookingCreateDto.getEnd().equals(bookingCreateDto.getStart())) {
            throw new ValidationException("End date must be after start date");
        }

        // Load entities
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + bookerId));

        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + bookingCreateDto.getItemId()));

        // Validate item is available
        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        // Validate booker is not the owner
        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Owner cannot book their own item");
        }

        // Create booking
        Booking booking = Booking.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Created booking with id: {}", savedBooking.getId());

        return BookingMapper.toResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        // Validate user is the owner
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Only item owner can approve booking");
        }

        // Validate booking status
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Only WAITING bookings can be approved or rejected");
        }

        // Update status
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Booking {} {} by owner {}", bookingId, approved ? "approved" : "rejected", ownerId);
        return BookingMapper.toResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        // Validate user is booker or owner
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Booking not found with id: " + bookingId);
        }

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long bookerId, BookingState state) {
        // Validate user exists
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + bookerId));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerId(bookerId, sort);
            case CURRENT ->
                    bookingRepository.findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(bookerId, now, now, sort);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(bookerId, now, sort);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(bookerId, now, sort);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, sort);
        };

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, BookingState state) {
        // Validate user exists
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + ownerId));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItemOwnerId(ownerId, sort);
            case CURRENT ->
                    bookingRepository.findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqual(ownerId, now, now, sort);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(ownerId, now, sort);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfter(ownerId, now, sort);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, sort);
        };

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}