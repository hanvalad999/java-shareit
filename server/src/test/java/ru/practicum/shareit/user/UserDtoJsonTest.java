package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testSerialization() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        var result = json.write(userDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john@example.com");
    }

    @Test
    void testDeserialization() throws Exception {
        String content = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@example.com\"}";

        UserDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }
}