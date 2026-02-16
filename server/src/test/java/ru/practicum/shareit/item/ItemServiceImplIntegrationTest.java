package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Long ownerId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        UserDto owner = userService.create(UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());
        ownerId = owner.getId();

        UserDto otherUser = userService.create(UserDto.builder()
                .name("Other User")
                .email("other@example.com")
                .build());
        otherUserId = otherUser.getId();
    }

    @Test
    void create_shouldCreateItem_whenValidData() {
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build();

        ItemDto created = itemService.create(ownerId, itemDto);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Drill");
        assertThat(created.getDescription()).isEqualTo("Power drill");
        assertThat(created.getAvailable()).isTrue();
        assertThat(created.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void create_shouldThrowNotFoundException_whenOwnerNotFound() {
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build();

        assertThatThrownBy(() -> itemService.create(999L, itemDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getById_shouldReturnItem_whenItemExists() {
        ItemDto created = itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());

        ItemDetailDto found = itemService.getById(created.getId(), ownerId);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Drill");
    }

    @Test
    void getById_shouldReturnItemWithBookings_whenUserIsOwner() {
        ItemDto itemDto = itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());

        // Create past booking
        User owner = userRepository.findById(ownerId).orElseThrow();
        User booker = userRepository.findById(otherUserId).orElseThrow();
        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();

        Booking pastBooking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(pastBooking);

        ItemDetailDto found = itemService.getById(itemDto.getId(), ownerId);

        assertThat(found.getLastBooking()).isNotNull();
        assertThat(found.getLastBooking().getId()).isEqualTo(pastBooking.getId());
    }

    @Test
    void getById_shouldNotShowBookings_whenUserIsNotOwner() {
        ItemDto itemDto = itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());

        ItemDetailDto found = itemService.getById(itemDto.getId(), otherUserId);

        assertThat(found.getLastBooking()).isNull();
        assertThat(found.getNextBooking()).isNull();
    }

    @Test
    void update_shouldUpdateItem_whenUserIsOwner() {
        ItemDto created = itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .build();

        ItemDto updated = itemService.update(ownerId, created.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Updated Drill");
        assertThat(updated.getDescription()).isEqualTo("Power drill"); // Unchanged
    }

    @Test
    void update_shouldThrowForbiddenException_whenUserIsNotOwner() {
        ItemDto created = itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());

        ItemDto updateDto = ItemDto.builder()
                .name("Hacked Drill")
                .build();

        assertThatThrownBy(() -> itemService.update(otherUserId, created.getId(), updateDto))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void delete_shouldDeleteItem_whenUserIsOwner() {
        ItemDto created = itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());

        itemService.delete(ownerId, created.getId());

        assertThatThrownBy(() -> itemService.getById(created.getId(), ownerId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void search_shouldReturnItems_whenTextMatches() {
        itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill for drilling")
                .available(true)
                .build());
        itemService.create(ownerId, ItemDto.builder()
                .name("Saw")
                .description("Hand saw")
                .available(true)
                .build());

        Collection<ItemDto> results = itemService.search("drill");

        assertThat(results).hasSize(1);
        assertThat(results.iterator().next().getName()).isEqualTo("Drill");
    }

    @Test
    void search_shouldReturnEmptyList_whenTextIsBlank() {
        itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());

        Collection<ItemDto> results = itemService.search("");

        assertThat(results).isEmpty();
    }

    @Test
    void addComment_shouldAddComment_whenUserHasCompletedBooking() {
        ItemDto itemDto = itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());

        // Create completed booking
        User booker = userRepository.findById(otherUserId).orElseThrow();
        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();

        Booking completedBooking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(completedBooking);

        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        CommentDto added = itemService.addComment(otherUserId, itemDto.getId(), commentDto);

        assertThat(added).isNotNull();
        assertThat(added.getId()).isNotNull();
        assertThat(added.getText()).isEqualTo("Great item!");
        assertThat(added.getAuthorName()).isEqualTo("Other User");
        assertThat(added.getCreated()).isNotNull();
    }

    @Test
    void addComment_shouldThrowValidationException_whenNoCompletedBooking() {
        ItemDto itemDto = itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        assertThatThrownBy(() -> itemService.addComment(otherUserId, itemDto.getId(), commentDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("complete booking");
    }

    @Test
    void getByOwnerIdWithBookings_shouldReturnOwnerItems() {
        itemService.create(ownerId, ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build());
        itemService.create(ownerId, ItemDto.builder()
                .name("Saw")
                .description("Hand saw")
                .available(true)
                .build());

        List<ItemWithBookingsDto> items = itemService.getByOwnerIdWithBookings(ownerId);

        assertThat(items).hasSize(2);
        assertThat(items).extracting(ItemWithBookingsDto::getName)
                .containsExactlyInAnyOrder("Drill", "Saw");
    }
}