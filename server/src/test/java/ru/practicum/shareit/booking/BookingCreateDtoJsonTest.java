package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    @Test
    void testSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 20, 10, 0, 0);

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2024-01-15T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2024-01-20T10:00:00");
    }

    @Test
    void testDeserialization_withValidDates() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2024-01-15T10:00:00\",\"end\":\"2024-01-20T10:00:00\"}";

        BookingCreateDto result = json.parse(content).getObject();

        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 0, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 20, 10, 0, 0));
    }

    @Test
    void testDateFormat() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2024-01-15T10:00:00\",\"end\":\"2024-01-20T10:00:00\"}";

        BookingCreateDto result = json.parse(content).getObject();

        assertThat(result.getStart()).isNotNull();
        assertThat(result.getEnd()).isNotNull();

        // Verify that dates are parsed correctly with the specified format
        var serialized = json.write(result);
        assertThat(serialized).extractingJsonPathStringValue("$.start").matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
    }
}