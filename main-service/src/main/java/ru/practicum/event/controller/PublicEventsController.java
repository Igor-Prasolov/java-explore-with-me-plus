package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventsController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> findPublicEventByParam(@RequestParam(required = false) String text,
                                                      @RequestParam(required = false) List<Long> categories,
                                                      @RequestParam(required = false) Boolean paid,
                                                      @RequestParam(required = false)
                                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                      @RequestParam(required = false)
                                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                      @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                                      @RequestParam(required = false) String sort,
                                                      @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                                      @RequestParam(required = false, defaultValue = "10") @Min(1) int size,
                                                      HttpServletRequest request) {
        log.info("call findPublicEventByParam from the controller");

        List<EventShortDto> eventShortDtoList = eventService.findPublicEventByParam(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
        eventService.saveHit(request.getRequestURI(), request.getRemoteAddr());

        return eventShortDtoList;
    }

    @GetMapping("/{id}")
    public EventFullDto findPublicEventById(@PathVariable Long id,
                                            HttpServletRequest request) {
        log.info("call findPublicEventById from the controller");

        EventFullDto eventFullDto = eventService.findPublicEventById(id);
        eventService.saveHit(request.getRequestURI(), request.getRemoteAddr());

        return eventFullDto;
    }
}
