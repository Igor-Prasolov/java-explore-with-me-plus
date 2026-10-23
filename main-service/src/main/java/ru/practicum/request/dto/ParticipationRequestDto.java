package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")//я хз тут по спецификации формат прописан 2022-09-06T21:10:05.432
    private LocalDateTime created;

    private Long event;

    private Long requester;

    private String status;// тут тоже хз, по идее надо enum-класс со статусами ожидание, отклонено и опубликовано
}