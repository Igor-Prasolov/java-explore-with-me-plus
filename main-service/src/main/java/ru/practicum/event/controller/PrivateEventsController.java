package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventsController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> findAllEventsByInitiatorId(@PathVariable Long userId,
                                                     @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                                     @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        log.info("call findAllEventsByInitiatorId from the controller");

        return eventService.findAllEventsByInitiatorId(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findEventByInitiatorId(@PathVariable Long userId,
                                                @PathVariable Long eventId) {
        log.info("call findEventByInitiatorId from the controller");

        return eventService.findEventByInitiatorId(userId, eventId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @Valid @RequestBody NewEventDto newEventDto) {
        log.info("call createEvent from the controller");

        return eventService.createEvent(newEventDto, userId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventForUser(@PathVariable Long userId,
                                           @PathVariable Long eventId,
                                           @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("call updateEventForUser from the controller");

        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

}
