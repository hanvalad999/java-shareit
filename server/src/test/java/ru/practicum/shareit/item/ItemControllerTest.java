package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void create_shouldReturnCreatedItem_whenValidData() throws Exception {
        ItemDto inputDto = ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build();
        ItemDto outputDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .ownerId(1L)
                .build();

        when(itemService.create(eq(1L), any(ItemDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("Power drill"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void getById_shouldReturnItem_whenItemExists() throws Exception {
        ItemDetailDto itemDto = ItemDetailDto.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .comments(Collections.emptyList())
                .build();

        when(itemService.getById(1L, 1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));

        verify(itemService).getById(1L, 1L);
    }

    @Test
    void getById_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        when(itemService.getById(999L, 1L))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/999")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByOwner_shouldReturnOwnerItems() throws Exception {
        ItemWithBookingsDto item1 = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build();
        ItemWithBookingsDto item2 = ItemWithBookingsDto.builder()
                .id(2L)
                .name("Saw")
                .description("Hand saw")
                .available(true)
                .build();

        when(itemService.getByOwnerIdWithBookings(1L)).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemService).getByOwnerIdWithBookings(1L);
    }

    @Test
    void update_shouldReturnUpdatedItem() throws Exception {
        ItemDto inputDto = ItemDto.builder()
                .name("Updated Drill")
                .build();
        ItemDto outputDto = ItemDto.builder()
                .id(1L)
                .name("Updated Drill")
                .description("Power drill")
                .available(true)
                .build();

        when(itemService.update(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(outputDto);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Drill"));

        verify(itemService).update(eq(1L), eq(1L), any(ItemDto.class));
    }

    @Test
    void update_shouldReturnForbidden_whenUserIsNotOwner() throws Exception {
        ItemDto inputDto = ItemDto.builder()
                .name("Hacked Drill")
                .build();

        when(itemService.update(eq(2L), eq(1L), any(ItemDto.class)))
                .thenThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_shouldReturnNoContent_whenItemDeleted() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNoContent());

        verify(itemService).delete(1L, 1L);
    }

    @Test
    void search_shouldReturnItems_whenTextMatches() throws Exception {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build();

        when(itemService.search("drill")).thenReturn(List.of(item));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));

        verify(itemService).search("drill");
    }

    @Test
    void search_shouldReturnEmptyList_whenTextIsBlank() throws Exception {
        when(itemService.search("")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        CommentDto inputDto = CommentDto.builder()
                .text("Great item!")
                .build();
        CommentDto outputDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("John Doe")
                .created(LocalDateTime.now())
                .build();

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("John Doe"));

        verify(itemService).addComment(eq(1L), eq(1L), any(CommentDto.class));
    }

    @Test
    void addComment_shouldReturnBadRequest_whenUserHasNoCompletedBooking() throws Exception {
        CommentDto inputDto = CommentDto.builder()
                .text("Great item!")
                .build();

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenThrow(new ValidationException("User must have completed booking"));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }
}