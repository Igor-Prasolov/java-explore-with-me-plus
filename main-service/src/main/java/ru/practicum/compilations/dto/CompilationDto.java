package ru.practicum.compilations.dto;

import lombok.Data;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Data
public class CompilationDto { //отдаем пользователю на гет

    private List<EventShortDto> events;
    private Long id;
    private Boolean pinned;
    private String title;

}
