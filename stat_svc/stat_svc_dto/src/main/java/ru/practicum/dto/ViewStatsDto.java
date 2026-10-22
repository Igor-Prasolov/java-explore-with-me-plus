package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// Отправляет пользователю в Get /stats эндпоинте
@Data
@AllArgsConstructor
public class ViewStatsDto {

    private String app;

    private String uri;

    private Long hits;
}
