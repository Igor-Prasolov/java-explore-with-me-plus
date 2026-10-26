package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.entity.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.entity.Event;
import ru.practicum.event.entity.State;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidateException;
import ru.practicum.page.OffsetPageRequest;
import ru.practicum.user.entity.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Override
    @Transactional
    public EventFullDto createEvent(NewEventDto newEventDto, Long initiatorId) {

        User initiator = userRepository.findById(initiatorId) // не взял из сервиса, потому что он возвращает UserDto
                .orElseThrow(() -> new NotFoundException("User with id =" + initiatorId + " not found"));

        Event event = eventMapper.toEvent(newEventDto);

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidateException("For the requested operation the conditions are not met");
        }

        event.setInitiator(initiator);
        event.setState(State.PENDING);
        Category category = categoryRepository.findById(newEventDto.getCategory()) // не взял из сервиса, потому что там pageable второй параметр
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " not found"));
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

        return events.stream()
                .map(eventMapper::toEventShort)
                .toList();
    }

    @Override
    public EventFullDto findEventByInitiatorId(Long initiatorId, Long eventId) {
        userService.getUserById(initiatorId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        if (!event.getInitiator().getId().equals(initiatorId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found");
        }
        // пока не из чего заполнять поля view и confirmedRequests, нужна реализация request


        return eventMapper.toEventFullDto(event);
    }

}
