package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.entity.Event;
import ru.practicum.event.entity.State;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> findAllEventsByInitiatorId(Long initiatorId, int from, int size);

    EventFullDto createEvent(NewEventDto newEventDto, Long initiatorId);

    EventFullDto findEventByInitiatorId(Long initiatorId, Long eventId);

    EventFullDto updateEvent(Long initiatorId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventFullDto> findEventAdminByParam(List<Long> users, List<State> states, List<Long> categories,
                                       LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> findPublicEventByParam(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size);

    EventFullDto findPublicEventById(Long eventId);

    Event findEventById(Long eventId);

    void saveHit(String uri, String ip);

}
