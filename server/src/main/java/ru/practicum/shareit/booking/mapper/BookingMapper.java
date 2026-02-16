package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@UtilityClass
public class BookingMapper {

    public static BookingResponseDto toResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(toItemShortDto(booking.getItem()))
                .booker(toUserShortDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }

    private static BookingResponseDto.ItemShortDto toItemShortDto(Item item) {
        return BookingResponseDto.ItemShortDto.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }

    private static BookingResponseDto.UserShortDto toUserShortDto(User user) {
        return BookingResponseDto.UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}