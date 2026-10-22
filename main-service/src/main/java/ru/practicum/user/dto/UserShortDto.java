package ru.practicum.user.dto;

import lombok.Data;

@Data
public class UserShortDto { // тоже отдаем короткую информацию в контроллерах гет

    private Long id;

    private String name;
}
