package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;

    @Test
    void create_shouldReturnCreatedBooking_whenValidData() throws Exception {
        BookingCreateDto inputDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2024, 1, 15, 10, 0, 0))
                .end(LocalDateTime.of(2024, 1, 20, 10, 0, 0))
                .build();
        BookingResponseDto outputDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2024, 1, 15, 10, 0, 0))
                .end(LocalDateTime.of(2024, 1, 20, 10, 0, 0))
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.create(eq(1L), any(BookingCreateDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.start").value("2024-01-15T10:00:00"))
                .andExpect(jsonPath("$.end").value("2024-01-20T10:00:00"));

        verify(bookingService).create(eq(1L), any(BookingCreateDto.class));
    }

    @Test
    void create_shouldReturnBadRequest_whenEndBeforeStart() throws Exception {
        BookingCreateDto inputDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        when(bookingService.create(eq(1L), any(BookingCreateDto.class)))
                .thenThrow(new ValidationException("End date must be after start date"));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_shouldReturnApprovedBooking() throws Exception {
        BookingResponseDto outputDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.approve(1L, 1L, true)).thenReturn(outputDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).approve(1L, 1L, true);
    }

    @Test
    void approve_shouldReturnRejectedBooking_whenApprovedIsFalse() throws Exception {
        BookingResponseDto outputDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.REJECTED)
                .build();

        when(bookingService.approve(1L, 1L, false)).thenReturn(outputDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(bookingService).approve(1L, 1L, false);
    }

    @Test
    void getById_shouldReturnBooking_whenUserHasAccess() throws Exception {
        BookingResponseDto outputDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.getById(1L, 1L)).thenReturn(outputDto);

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookingService).getById(1L, 1L);
    }

    @Test
    void getById_shouldReturnNotFound_whenBookingDoesNotExist() throws Exception {
        when(bookingService.getById(1L, 999L))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/999")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllByBooker_shouldReturnBookings_withStateAll() throws Exception {
        BookingResponseDto booking1 = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();
        BookingResponseDto booking2 = BookingResponseDto.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.getAllByBooker(1L, BookingState.ALL))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingService).getAllByBooker(1L, BookingState.ALL);
    }

    @Test
    void getAllByBooker_shouldReturnBookings_withDefaultState() throws Exception {
        when(bookingService.getAllByBooker(1L, BookingState.ALL))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(bookingService).getAllByBooker(1L, BookingState.ALL);
    }

    @Test
    void getAllByBooker_shouldReturnWaitingBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.getAllByBooker(1L, BookingState.WAITING))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        verify(bookingService).getAllByBooker(1L, BookingState.WAITING);
    }

    @Test
    void getAllByOwner_shouldReturnBookingsForOwnerItems() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.getAllByOwner(1L, BookingState.ALL))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookingService).getAllByOwner(1L, BookingState.ALL);
    }

    @Test
    void getAllByOwner_shouldReturnWaitingBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.getAllByOwner(1L, BookingState.WAITING))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        verify(bookingService).getAllByOwner(1L, BookingState.WAITING);
    }
}