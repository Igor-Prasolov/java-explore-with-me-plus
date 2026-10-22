package ru.practicum.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUserRequest { // это приходит из контроллеров пост на создание юзера

    @NotBlank
    @Size(max = 254, min = 2)
    private String email;

    @NotBlank
    @Size(max = 250, min = 2)
    private String name;
}
