package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        UserDto owner = userService.create(UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());
        ownerId = owner.getId();

        UserDto booker = userService.create(UserDto.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());
        bookerId = booker.getId();

        ItemDto item = itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());
        itemId = item.getId();
    }

    @Test
    void create_shouldCreateBooking_whenItemIsAvailable() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto created = bookingService.create(bookerId, dto);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getItem().getId()).isEqualTo(itemId);
        assertThat(created.getBooker().getId()).isEqualTo(bookerId);
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void create_shouldThrowValidationException_whenEndBeforeStart() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1)) // End before start
                .build();

        assertThatThrownBy(() -> bookingService.create(bookerId, dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date");
    }

    @Test
    void create_shouldThrowNotFoundException_whenOwnerTriesToBookOwnItem() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.create(ownerId, dto)) // Owner tries to book
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Owner cannot book");
    }

    @Test
    void create_shouldThrowValidationException_whenItemNotAvailable() {
        ItemDto unavailableItem = itemService.create(ownerId, ItemDto.builder()
                .name("Unavailable Drill")
                .description("Not available")
                .available(false)
                .build());

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(unavailableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.create(bookerId, dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void approve_shouldApproveBooking_whenOwnerApproves() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto created = bookingService.create(bookerId, dto);

        BookingResponseDto approved = bookingService.approve(ownerId, created.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_shouldRejectBooking_whenOwnerRejects() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto created = bookingService.create(bookerId, dto);

        BookingResponseDto rejected = bookingService.approve(ownerId, created.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_shouldThrowValidationException_whenBookingNotWaiting() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto created = bookingService.create(bookerId, dto);
        bookingService.approve(ownerId, created.getId(), true); // Already approved

        assertThatThrownBy(() -> bookingService.approve(ownerId, created.getId(), true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("WAITING");
    }

    @Test
    void getById_shouldReturnBooking_whenUserIsOwner() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto created = bookingService.create(bookerId, dto);

        BookingResponseDto found = bookingService.getById(ownerId, created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
    }

    @Test
    void getById_shouldReturnBooking_whenUserIsBooker() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto created = bookingService.create(bookerId, dto);

        BookingResponseDto found = bookingService.getById(bookerId, created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
    }

    @Test
    void getById_shouldThrowNotFoundException_whenUserIsNeitherOwnerNorBooker() {
        UserDto otherUser = userService.create(UserDto.builder()
                .name("Other")
                .email("other@example.com")
                .build());

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto created = bookingService.create(bookerId, dto);

        assertThatThrownBy(() -> bookingService.getById(otherUser.getId(), created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByBooker_shouldReturnBookings_withStateAll() {
        BookingCreateDto dto1 = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.create(bookerId, dto1);

        List<BookingResponseDto> bookings = bookingService.getAllByBooker(bookerId, BookingState.ALL);

        assertThat(bookings).hasSize(1);
    }

    @Test
    void getAllByBooker_shouldReturnWaitingBookings_withStateWaiting() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.create(bookerId, dto);

        List<BookingResponseDto> bookings = bookingService.getAllByBooker(bookerId, BookingState.WAITING);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getAllByOwner_shouldReturnBookings_forOwnerItems() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.create(bookerId, dto);

        List<BookingResponseDto> bookings = bookingService.getAllByOwner(ownerId, BookingState.ALL);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(itemId);
    }

    @Test
    void getAllByOwner_shouldReturnEmptyList_whenOwnerHasNoItems() {
        UserDto newOwner = userService.create(UserDto.builder()
                .name("New Owner")
                .email("newowner@example.com")
                .build());

        List<BookingResponseDto> bookings = bookingService.getAllByOwner(newOwner.getId(), BookingState.ALL);

        assertThat(bookings).isEmpty();
    }
}