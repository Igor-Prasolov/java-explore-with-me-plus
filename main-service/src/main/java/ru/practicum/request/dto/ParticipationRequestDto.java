package ru.practicum.request.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {

    private LocalDateTime created; //я хз тут по спецификации формат прописан 2022-09-06T21:10:05.432
    private Long event;
    private Long id;
    private Long requester;
    private String status; // тут тоже хз, по идее надо enum-класс со статусами ожидание, отклонено и опубликовано
}
