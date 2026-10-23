package ru.practicum.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events/{eventId}/requests")
public class EventRequestController {

    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/requests", userId, eventId);
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateEventRequestsStatus(@PathVariable Long userId,
                                                                    @PathVariable Long eventId,
                                                                    @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("PATCH /users/{}/events/{}/requests", userId, eventId);
        return requestService.updateEventRequestsStatus(userId, eventId, updateRequest);
    }
}