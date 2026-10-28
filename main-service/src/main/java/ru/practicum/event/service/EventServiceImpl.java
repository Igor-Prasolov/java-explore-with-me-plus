package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.category.entity.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.entity.Event;
import ru.practicum.event.entity.State;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidateException;
import ru.practicum.page.OffsetPageRequest;
import ru.practicum.request.entity.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.entity.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private final EventMapper eventMapper;

    private final UserService userService;

    private final StatsClient statsClient;


    @Override
    @Transactional
    public EventFullDto createEvent(NewEventDto newEventDto, Long initiatorId) {

        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> {
                    log.warn("WARNING createEvent: user not found");
                    return NotFoundException.of(User.class, initiatorId);
                });

        Event event = eventMapper.toEvent(newEventDto);

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("WARNING createEvent: event date must be at least 2 hours from now");
            throw new IllegalArgumentException("For the requested operation the conditions are not met");
        }

        event.setInitiator(initiator);
        event.setState(State.PENDING);
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> {
                    log.warn("WARNING createEvent: category not found");
                    return NotFoundException.of(Category.class, newEventDto.getCategory());
                });

        event.setCategory(category);

        eventRepository.save(event);

        EventFullDto saved = eventMapper.toEventFullDto(event);
        saved.setConfirmedRequests(0L);
        saved.setViews(0L);

        return saved;
    }

    @Override
    public List<EventShortDto> findAllEventsByInitiatorId(Long initiatorId, int from, int size) {
        userService.getUserById(initiatorId);

        Pageable pageable = new OffsetPageRequest(from, size);
        List<Event> events = eventRepository.findByInitiatorId(initiatorId, pageable);

        List<EventShortDto> eventShortDtoList = events.stream()
                .map(eventMapper::toEventShort)
                .toList();

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Long> views = getViewsForEvents(eventIds);

        Map<Long, Long> confirmed = getConfirmedRequestsForEvents(eventIds);

        for (EventShortDto ev : eventShortDtoList) {
            ev.setConfirmedRequests(confirmed.getOrDefault(ev.getId(), 0L));
            ev.setViews(views.getOrDefault(ev.getId(), 0L));
        }

        return eventShortDtoList;
    }

    @Override
    public EventFullDto findEventByInitiatorId(Long initiatorId, Long eventId) {
        userService.getUserById(initiatorId);
        Event event = findEventById(eventId);

        if (!event.getInitiator().getId().equals(initiatorId)) {
            log.warn("WARNING findEventByInitiatorId: user is not the initiator");
            throw NotFoundException.of(Event.class, eventId);
        }

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));

        Map<Long, Long> views = getViewsForEvents(List.of(eventId));
        eventFullDto.setViews(views.getOrDefault(eventId, 0L));

        return eventFullDto;
    }

    @Override
    public List<EventFullDto> findEventAdminByParam(List<Long> users, List<State> states, List<Long> categories,
                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        if (rangeStart == null) {
            rangeStart = LocalDateTime.of(2000, 1, 1, 0, 0);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.of(2100, 1, 1, 0, 0);
        }

        Pageable pageable = new OffsetPageRequest(from, size);

        List<Long> userdIdList;
        if (users == null || users.isEmpty()) {
            userdIdList = userRepository.findAll().stream()
                    .map(User::getId)
                    .toList();
        } else {
            userdIdList = users;
        }
        if (userdIdList.isEmpty()) {
            return List.of();
        }

        List<State> statesList;
        if (states == null || states.isEmpty()) {
            statesList = List.of(State.values());
        } else {
            statesList = states;
        }

        List<Long> categoryIdList;
        if (categories == null || categories.isEmpty()) {
            categoryIdList = categoryRepository.findAll().stream()
                    .map(Category::getId)
                    .toList();
        } else {
            categoryIdList = categories;
        }
        if (categoryIdList.isEmpty()) {
            return List.of();
        }

        List<Event> eventList = eventRepository.findEventByParam(userdIdList, statesList, categoryIdList,
                rangeStart, rangeEnd, pageable);

        List<EventFullDto> eventFullDtoList = eventList.stream()
                .map(eventMapper::toEventFullDto)
                .toList();

        List<Long> eventIds = eventList.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Long> views = getViewsForEvents(eventIds);

        Map<Long, Long> confirmed = getConfirmedRequestsForEvents(eventIds);

        for (EventFullDto ev : eventFullDtoList) {
            ev.setConfirmedRequests(confirmed.getOrDefault(ev.getId(), 0L));
            ev.setViews(views.getOrDefault(ev.getId(), 0L));
        }

        return eventFullDtoList;
    }

    @Override
    public List<EventShortDto> findPublicEventByParam(String text, List<Long> categories, Boolean paid,
                                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                                      String sort, int from, int size) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                log.warn("WARNING findPublicEventByParam: rangeEnd must not be before rangeStart");
                throw new IllegalArgumentException("rangeEnd must not be before rangeStart");
            }
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.of(2100, 1, 1, 0, 0);
        }

        if (text == null) {
            text = "%%";
        }

        List<Long> categoryIdList;
        if (categories == null || categories.isEmpty()) {
            categoryIdList = categoryRepository.findAll().stream()
                    .map(Category::getId)
                    .toList();
        } else {
            categoryIdList = categories;
        }
        if (categoryIdList.isEmpty()) {
            return List.of();
        }

        List<Boolean> paidValues = paid == null
                ? List.of(true, false)
                : List.of(paid);


        Pageable pageable = new OffsetPageRequest(from, size);
        List<Event> eventList = eventRepository.findPublicEventByParam(State.PUBLISHED, text, categoryIdList, paidValues,
                rangeStart, rangeEnd, onlyAvailable, RequestStatus.CONFIRMED, pageable);

        List<EventShortDto> eventShortDtoList = eventList.stream()
                .map(eventMapper::toEventShort)
                .toList();

        List<Long> eventIds = eventList.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Long> views = getViewsForEvents(eventIds);

        Map<Long, Long> confirmed = getConfirmedRequestsForEvents(eventIds);

        for (EventShortDto ev : eventShortDtoList) {
            ev.setConfirmedRequests(confirmed.getOrDefault(ev.getId(), 0L));
            ev.setViews(views.getOrDefault(ev.getId(), 0L));
        }

        if (sort != null) {
            switch (sort) {
                case "EVENT_DATE" -> {
                } // запрос в репе уже под это
                case "VIEWS" -> {
                    return eventShortDtoList.stream()
                            .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                            .toList();
                }
                default -> {
                    log.warn("WARNING findPublicEventByParam: unknown sort value");
                    throw new IllegalArgumentException("Unknown sort value");
                }
            }
        }

        return eventShortDtoList;
    }

    @Override
    public EventFullDto findPublicEventById(Long eventId) {
        Event event = findEventById(eventId);
        if (!event.getState().equals(State.PUBLISHED)) {
            log.warn("WARNING findPublicEventById: Event state: {}", event.getState());
            throw NotFoundException.of(Event.class, eventId);
        }
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));

        Map<Long, Long> views = getViewsForEvents(List.of(eventId));
        eventFullDto.setViews(views.getOrDefault(eventId, 0L));

        return eventFullDto;
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(Long initiatorId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        userService.getUserById(initiatorId);
        Event event = findEventById(eventId);

        if (!event.getInitiator().getId().equals(initiatorId)) {
            log.warn("WARNING updateEvent: Only the initiator can change the event.");
            throw NotFoundException.of(Event.class, eventId);
        }

        if (!event.getState().equals(State.CANCELED) && !event.getState().equals(State.PENDING)) {
            log.warn("WARNING updateEvent: Events can only be modified if they have the cancelled and pending status");
            throw ValidateException.of(ErrorMessages.EVENT_NOT_PENDING_OR_CANCELED);
        }


        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                log.warn("WARNING updateEvent: Event date must be at least 2 hours from now");
                throw new IllegalArgumentException("Event date must be at least 2 hours from now");
            }
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> {
                        log.warn("WARNING updateEvent: category not found");
                        return NotFoundException.of(Category.class, updateEventUserRequest.getCategory());
                    });
            event.setCategory(category);
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getLocation() != null) {
            event.setLat(updateEventUserRequest.getLocation().getLat());
            event.setLon(updateEventUserRequest.getLocation().getLon());
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
            }
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        eventRepository.save(event);

        EventFullDto saved = eventMapper.toEventFullDto(event);
        saved.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));

        Map<Long, Long> views = getViewsForEvents(List.of(eventId));
        saved.setViews(views.getOrDefault(eventId, 0L));

        return saved;
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {

        Event event = findEventById(eventId);

        if (updateEventAdminRequest.getEventDate() != null) {
            if (updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                log.warn("WARNING updateAdminEvent: updateEvent date must be at least 1 hours from now");
                throw new IllegalArgumentException("Event date must be at least 1 hours from now");
            }
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> {
                        log.warn("WARNING updateAdminEvent: category not found");
                        return NotFoundException.of(Category.class, updateEventAdminRequest.getCategory());
                    });
            event.setCategory(category);
        }

        if (updateEventAdminRequest.getStateAction() != null) {
            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        log.warn("WARNING updateAdminEvent: Event date must be at least 1 hours from now");
                        throw ValidateException.of(ErrorMessages.EVENT_DATE_TOO_SOON);
                    }
                    if (!event.getState().equals(State.PENDING)) {
                        log.warn("WARNING updateAdminEvent: Cannot publish the event because it's not in the right state: {}", event.getState());
                        throw ValidateException.of(ErrorMessages.CANNOT_PUBLISH_EVENT, event.getState());
                    }
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState().equals(State.PUBLISHED)) {
                        log.warn("WARNING updateAdminEvent: Cannot reject the event because it's already published");
                        throw ValidateException.of(ErrorMessages.CANNOT_REJECT_PUBLISHED_EVENT);
                    }
                    event.setState(State.CANCELED);
                }
            }
        }

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLon(updateEventAdminRequest.getLocation().getLon());
            event.setLat(updateEventAdminRequest.getLocation().getLat());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        eventRepository.save(event);

        EventFullDto saved = eventMapper.toEventFullDto(event);
        saved.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));

        Map<Long, Long> views = getViewsForEvents(List.of(eventId));
        saved.setViews(views.getOrDefault(eventId, 0L));

        return saved;
    }


    @Override
    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("WARNING Repository findEventById: event not found");
                    return NotFoundException.of(Event.class, eventId);
                });
    }

    @Override
    public void saveHit(String uri, String ip) {
        EndpointHitDto hitDto = new EndpointHitDto();
        hitDto.setApp("ewm-main-service");
        hitDto.setUri(uri);
        hitDto.setIp(ip);
        hitDto.setTimestamp(LocalDateTime.now());
        statsClient.saveHit(hitDto);
    }

    private Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        List<String> uris = new ArrayList<>();
        for (Long id : eventIds) {
            uris.add("/events/" + id);
        }
        if (uris.isEmpty()) {
            return Map.of();
        }
        List<ViewStatsDto> viewStatsDtoList = statsClient.findStats(LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.now(), uris, true);

        Map<String, Long> hits = new HashMap<>();
        Map<Long, Long> result = new HashMap<>();

        for (ViewStatsDto dto : viewStatsDtoList) {
            hits.put(dto.getUri(), dto.getHits());
        }

        for (Long id : eventIds) {
            String key = "/events/" + id;
            long res = hits.getOrDefault(key, 0L);
            result.put(id, res);
        }

        return result;
    }

    private Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> confirmedRequestList = requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);

        Map<Long, Long> res = new HashMap<>();

        for (Object[] row : confirmedRequestList) {
            Long eventId = (Long) row[0];
            Long count = (Long) row[1];
            res.put(eventId, count);
        }

        return res;
    }

}
