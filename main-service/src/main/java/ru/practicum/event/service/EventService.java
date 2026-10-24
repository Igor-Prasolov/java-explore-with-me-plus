package ru.practicum.event.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;

import java.util.List;

public interface EventService {

    List<EventShortDto> findAllEventsByInitiatorId(Long initiatorId, int from, int size);

    EventFullDto createEvent(NewEventDto newEventDto, Long initiatorId);

    EventFullDto findEventByInitiatorId(Long initiatorId, Long eventId);
}
