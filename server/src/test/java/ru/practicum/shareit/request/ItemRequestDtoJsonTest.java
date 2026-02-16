package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testSerialization() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a power drill")
                .build();

        var result = json.write(requestDto);

        assertThat(result).hasJsonPath("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a power drill");
    }

    @Test
    void testDeserialization() throws Exception {
        String content = "{\"description\":\"Need a power drill\"}";

        ItemRequestDto result = json.parse(content).getObject();

        assertThat(result.getDescription()).isEqualTo("Need a power drill");
    }
}