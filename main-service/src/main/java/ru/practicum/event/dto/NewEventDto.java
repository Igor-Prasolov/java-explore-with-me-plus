package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewEventDto { // приходит от пользователя при создании

    @NotBlank
    @Size(max = 2000, min = 20)
    private String annotation;

    @NotNull
    private Long category;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    @Valid // спринг залезет в класс локации и посмотрит там два аннотированых поля
    private Location location;

    private Boolean paid = false; // по дефолту, что бы не присваивать в мапере

    private Integer participantLimit = 0; // тоже самое

    private Boolean requestModeration = true; // тоже самое

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

}
