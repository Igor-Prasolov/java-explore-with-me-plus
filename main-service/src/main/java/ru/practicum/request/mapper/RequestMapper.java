package ru.practicum.request.mapper;

import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.entity.Request;

import java.util.List;

public class RequestMapper {

    public static ParticipationRequestDto toDto(Request request) {
        if (request == null) {
            return null;
        }
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .build();
    }

    public static List<ParticipationRequestDto> toDtoList(List<Request> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(RequestMapper::toDto)
                .toList();
    }
}