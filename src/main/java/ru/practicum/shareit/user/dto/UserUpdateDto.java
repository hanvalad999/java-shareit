package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    private String name;

    @Email
    private String email;
}