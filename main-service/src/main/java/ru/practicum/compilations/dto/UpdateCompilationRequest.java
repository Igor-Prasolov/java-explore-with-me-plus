package ru.practicum.compilations.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCompilationRequest { // опциональная замена, приходит от пользователя

    private List<Long> events;
    private Boolean pinned;

    @Size(min = 1, max = 50)
    private String title;

}
