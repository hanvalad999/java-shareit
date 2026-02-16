package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long requestorId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        UserDto requestor = userService.create(UserDto.builder()
                .name("Requestor")
                .email("requestor@example.com")
                .build());
        requestorId = requestor.getId();

        UserDto otherUser = userService.create(UserDto.builder()
                .name("Other User")
                .email("other@example.com")
                .build());
        otherUserId = otherUser.getId();
    }

    @Test
    void create_shouldCreateRequest_whenValidData() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a power drill")
                .build();

        ItemRequestResponseDto created = itemRequestService.create(requestorId, dto);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a power drill");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void create_shouldThrowNotFoundException_whenUserNotFound() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a power drill")
                .build();

        assertThatThrownBy(() -> itemRequestService.create(999L, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id");
    }

    @Test
    void getByRequestor_shouldReturnRequestsWithItems() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();
        ItemRequestResponseDto request = itemRequestService.create(requestorId, requestDto);

        // Create item in response to request
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .requestId(request.getId())
                .build();
        itemService.create(otherUserId, itemDto);

        List<ItemRequestResponseDto> requests = itemRequestService.getByRequestor(requestorId);

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getId()).isEqualTo(request.getId());
        assertThat(requests.get(0).getItems()).hasSize(1);
        assertThat(requests.get(0).getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getByRequestor_shouldReturnEmptyList_whenUserHasNoRequests() {
        List<ItemRequestResponseDto> requests = itemRequestService.getByRequestor(otherUserId);

        assertThat(requests).isEmpty();
    }

    @Test
    void getByRequestor_shouldReturnRequestsOrderedByCreated() {
        ItemRequestDto request1 = ItemRequestDto.builder()
                .description("Need drill")
                .build();
        ItemRequestDto request2 = ItemRequestDto.builder()
                .description("Need saw")
                .build();

        ItemRequestResponseDto created1 = itemRequestService.create(requestorId, request1);
        ItemRequestResponseDto created2 = itemRequestService.create(requestorId, request2);

        List<ItemRequestResponseDto> requests = itemRequestService.getByRequestor(requestorId);

        assertThat(requests).hasSize(2);
        // Should be ordered by created date descending (newest first)
        assertThat(requests.get(0).getId()).isEqualTo(created2.getId());
        assertThat(requests.get(1).getId()).isEqualTo(created1.getId());
    }

    @Test
    void getAll_shouldReturnOtherUsersRequests() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();
        itemRequestService.create(requestorId, requestDto);

        List<ItemRequestResponseDto> requests = itemRequestService.getAll(otherUserId);

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getDescription()).isEqualTo("Need a drill");
    }

    @Test
    void getAll_shouldNotReturnOwnRequests() {
        ItemRequestDto ownRequest = ItemRequestDto.builder()
                .description("My request")
                .build();
        itemRequestService.create(requestorId, ownRequest);

        ItemRequestDto otherRequest = ItemRequestDto.builder()
                .description("Other request")
                .build();
        itemRequestService.create(otherUserId, otherRequest);

        List<ItemRequestResponseDto> requests = itemRequestService.getAll(requestorId);

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getDescription()).isEqualTo("Other request");
    }

    @Test
    void getById_shouldReturnRequest_whenRequestExists() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();
        ItemRequestResponseDto created = itemRequestService.create(requestorId, requestDto);

        ItemRequestResponseDto found = itemRequestService.getById(requestorId, created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getDescription()).isEqualTo("Need a drill");
    }

    @Test
    void getById_shouldReturnRequestWithItems() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();
        ItemRequestResponseDto request = itemRequestService.create(requestorId, requestDto);

        // Add item to request
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .requestId(request.getId())
                .build();
        itemService.create(otherUserId, itemDto);

        ItemRequestResponseDto found = itemRequestService.getById(requestorId, request.getId());

        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getById_shouldThrowNotFoundException_whenRequestDoesNotExist() {
        assertThatThrownBy(() -> itemRequestService.getById(requestorId, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getById_shouldBeAccessibleByAnyUser() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();
        ItemRequestResponseDto created = itemRequestService.create(requestorId, requestDto);

        // Other user can view it
        ItemRequestResponseDto found = itemRequestService.getById(otherUserId, created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
    }
}