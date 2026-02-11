package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto.BookingDtoBuilder builder = BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus());

        if (booking.getItem() != null) {
            builder.itemId(booking.getItem().getId())
                    .item(new BookingDto.BookedItemDto(
                            booking.getItem().getId(),
                            booking.getItem().getName()
                    ));
        }
        if (booking.getBooker() != null) {
            builder.bookerId(booking.getBooker().getId())
                    .booker(new BookingDto.BookerDto(
                            booking.getBooker().getId()
                    ));
        }
        return builder.build();
    }

    public static Booking toBooking(BookingDto bookingDto, Item item, User booker) {
        if (bookingDto == null) {
            return null;
        }
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(bookingDto.getStatus())
                .build();
    }
}

