package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.event.entity.StateActionForUser;

import java.time.LocalDateTime;

@Data
public class UpdateEventUserRequest { // нет аннотаций на нул и тд, потому что частичное обновление

    @Size(min = 20, max = 2000)
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Valid
    private Location location;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateActionForUser stateAction;

    @Size(min = 3, max = 120)
    private String title;
}
