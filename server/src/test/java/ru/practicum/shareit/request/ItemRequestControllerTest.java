package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void create_shouldReturnCreatedRequest_whenValidData() throws Exception {
        ItemRequestDto inputDto = ItemRequestDto.builder()
                .description("Need a power drill")
                .build();
        ItemRequestResponseDto outputDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a power drill")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        when(itemRequestService.create(eq(1L), any(ItemRequestDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a power drill"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray());

        verify(itemRequestService).create(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    void create_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        ItemRequestDto inputDto = ItemRequestDto.builder()
                .description("Need a power drill")
                .build();

        when(itemRequestService.create(eq(999L), any(ItemRequestDto.class)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByRequestor_shouldReturnOwnRequests() throws Exception {
        ItemRequestResponseDto request1 = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need drill")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();
        ItemRequestResponseDto request2 = ItemRequestResponseDto.builder()
                .id(2L)
                .description("Need saw")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        when(itemRequestService.getByRequestor(1L)).thenReturn(List.of(request1, request2));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemRequestService).getByRequestor(1L);
    }

    @Test
    void getByRequestor_shouldReturnEmptyList_whenUserHasNoRequests() throws Exception {
        when(itemRequestService.getByRequestor(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(itemRequestService).getByRequestor(1L);
    }

    @Test
    void getAll_shouldReturnOtherUsersRequests() throws Exception {
        ItemRequestResponseDto request1 = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need drill")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();
        ItemRequestResponseDto request2 = ItemRequestResponseDto.builder()
                .id(2L)
                .description("Need saw")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        when(itemRequestService.getAll(1L)).thenReturn(List.of(request1, request2));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(itemRequestService).getAll(1L);
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoOtherRequests() throws Exception {
        when(itemRequestService.getAll(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(itemRequestService).getAll(1L);
    }

    @Test
    void getById_shouldReturnRequest_whenRequestExists() throws Exception {
        ItemRequestResponseDto request = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a power drill")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        when(itemRequestService.getById(1L, 1L)).thenReturn(request);

        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a power drill"));

        verify(itemRequestService).getById(1L, 1L);
    }

    @Test
    void getById_shouldReturnNotFound_whenRequestDoesNotExist() throws Exception {
        when(itemRequestService.getById(1L, 999L))
                .thenThrow(new NotFoundException("Request not found"));

        mockMvc.perform(get("/requests/999")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound());

        verify(itemRequestService).getById(1L, 999L);
    }

    @Test
    void getById_shouldReturnRequestWithItems() throws Exception {
        ItemShortDto item = ItemShortDto.builder()
                .id(1L)
                .name("Drill")
                .ownerId(2L)
                .build();
        ItemRequestResponseDto request = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a power drill")
                .created(LocalDateTime.now())
                .items(List.of(item))
                .build();

        when(itemRequestService.getById(1L, 1L)).thenReturn(request);

        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Drill"));

        verify(itemRequestService).getById(1L, 1L);
    }
}