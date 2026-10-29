package ru.practicum.compilations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NewCompilationDto { // приходит от пользователя

    private List<Long> events;

    private Boolean pinned = false; //дефолт

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

}
