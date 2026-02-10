package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.ItemBookingDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ru.practicum.shareit.user.UserRepository userRepository;
    private final ru.practicum.shareit.item.ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingCreateDto bookingCreateDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item not found: " + bookingCreateDto.getItemId()));

        if (item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Owner cannot book own item");
        }
        if (!item.getAvailable()) {
            throw new BadRequestException("Item is not available");
        }
        if (bookingCreateDto.getEnd().isBefore(bookingCreateDto.getStart())
                || bookingCreateDto.getEnd().equals(bookingCreateDto.getStart())) {
            throw new BadRequestException("End date must be after start date");
        }

        Booking booking = Booking.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .status(BookingStatus.WAITING)
                .item(item)
                .booker(booker)
                .build();
        booking = bookingRepository.save(booking);
        return toDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingId));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only owner can approve booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Booking is not in WAITING status");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);
        return toDto(booking);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingId));

        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();
        if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
            throw new AccessDeniedException("Access denied to this booking");
        }
        return toDto(booking);
    }

    @Override
    public List<BookingDto> getByBookerId(Long bookerId, BookingState state, int from, int size) {
        if (!userRepository.existsById(bookerId)) {
            throw new EntityNotFoundException("User not found: " + bookerId);
        }
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(bookerId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByBookerId(bookerId, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findPastByBookerId(bookerId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureByBookerId(bookerId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        bookerId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        bookerId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }
        return bookings.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getByOwnerId(Long ownerId, BookingState state, int from, int size) {
        if (!userRepository.existsById(ownerId)) {
            throw new EntityNotFoundException("User not found: " + ownerId);
        }
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByOwnerId(ownerId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByOwnerId(ownerId, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findPastByOwnerId(ownerId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureByOwnerId(ownerId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }
        return bookings.stream().map(this::toDto).collect(Collectors.toList());
    }

    private BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(BookerDto.builder().id(booking.getBooker().getId()).build())
                .item(ItemBookingDto.builder()
                        .id(booking.getItem().getId())
                        .name(booking.getItem().getName())
                        .build())
                .build();
    }
}
