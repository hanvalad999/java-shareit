package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testSerialization_withDateFormat() throws Exception {
        LocalDateTime created = LocalDateTime.of(2024, 1, 15, 10, 30, 45);

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("John Doe")
                .created(created)
                .build();

        var result = json.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Great item!");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2024-01-15T10:30:45");
    }

    @Test
    void testDeserialization() throws Exception {
        String content = "{\"id\":1,\"text\":\"Great item!\",\"authorName\":\"John Doe\"," +
                "\"created\":\"2024-01-15T10:30:45\"}";

        CommentDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Great item!");
        assertThat(result.getAuthorName()).isEqualTo("John Doe");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 45));
    }

    @Test
    void testDateFormat() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .text("Test comment")
                .authorName("Author")
                .created(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                .build();

        var result = json.write(commentDto);

        // Verify the date format matches the pattern yyyy-MM-dd'T'HH:mm:ss
        assertThat(result).extractingJsonPathStringValue("$.created")
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
    }
}