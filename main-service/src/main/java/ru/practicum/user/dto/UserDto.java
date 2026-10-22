package ru.practicum.user.dto;

import lombok.Data;

@Data
public class UserDto { // отдаем пользователю на геты в контроллерах

    private Long id;

    private String email;

    private String name;
}
