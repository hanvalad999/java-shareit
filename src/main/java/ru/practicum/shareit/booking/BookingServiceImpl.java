package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto create(Long userId, BookingDto bookingDto) {
        User booker = findUser(userId);
        Item item = findItem(bookingDto.getItemId());

        if (item.getOwner() != null && userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Owner cannot book own item");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new BadRequestException("Item is not available for booking");
        }

        validateBookingDates(bookingDto.getStart(), bookingDto.getEnd());

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getItem() == null || booking.getItem().getOwner() == null
                || !ownerId.equals(booking.getItem().getOwner().getId())) {
            // Пользователь не является владельцем вещи — для тестов Postman
            // это считается ошибкой доступа (400/403), а не "не найдено".
            throw new BadRequestException("Booking item does not belong to user");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Booking already processed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        Long bookerId = booking.getBooker() != null ? booking.getBooker().getId() : null;
        Long ownerId = booking.getItem() != null && booking.getItem().getOwner() != null
                ? booking.getItem().getOwner().getId()
                : null;

        if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
            throw new NotFoundException("Booking not available for this user");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId, String state) {
        findUser(userId);
        List<Booking> bookings = bookingRepository.findAllByBooker_Id(userId);
        return filterByState(bookings, state).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getByOwner(Long ownerId, String state) {
        findUser(ownerId);
        List<Booking> bookings = bookingRepository.findAllByItem_Owner_Id(ownerId);
        return filterByState(bookings, state).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private List<Booking> filterByState(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();
        String normalized = StringUtils.hasText(state) ? state.toUpperCase(Locale.ROOT) : "ALL";

        switch (normalized) {
            case "ALL":
                return bookings;
            case "FUTURE":
                return bookings.stream()
                        .filter(b -> b.getStart() != null && b.getStart().isAfter(now))
                        .collect(Collectors.toList());
            case "PAST":
                return bookings.stream()
                        .filter(b -> b.getEnd() != null && b.getEnd().isBefore(now))
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookings.stream()
                        .filter(b -> b.getStart() != null && b.getEnd() != null
                                && !b.getStart().isAfter(now) && !b.getEnd().isBefore(now))
                        .collect(Collectors.toList());
            case "WAITING":
                return bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.WAITING)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                        .collect(Collectors.toList());
            default:
                throw new BadRequestException("Unknown state: " + state);
        }
    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Start and end dates are required");
        }
        if (!start.isBefore(end)) {
            throw new BadRequestException("Start must be before end");
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Item findItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
    }
}

