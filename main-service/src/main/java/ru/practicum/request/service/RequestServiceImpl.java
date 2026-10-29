package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.entity.Event;
import ru.practicum.event.entity.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidateException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.entity.Request;
import ru.practicum.request.entity.RequestStatus;
import ru.practicum.request.entity.StatusForUpdateRequest;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.entity.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Getting requests for user id={}", userId);
        checkUserExists(userId);
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return RequestMapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Creating request: user id={}, event id={}", userId, eventId);
        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw ValidateException.of(ErrorMessages.REQUEST_ALREADY_SENT, userId, eventId);
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw ValidateException.of(ErrorMessages.INITIATOR_CANNOT_PARTICIPATE);
        }

        if (!State.PUBLISHED.equals(event.getState())) {
            throw ValidateException.of(ErrorMessages.UNPUBLISHED_EVENT);
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            throw ValidateException.of(ErrorMessages.PARTICIPANT_LIMIT_REACHED);
        }

        RequestStatus status = RequestStatus.PENDING;
        if (!Boolean.TRUE.equals(event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);
        request.setStatus(status);

        Request saved = requestRepository.save(request);
        return RequestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Canceling request id={} by user id={}", requestId, userId);
        checkUserExists(userId);

        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> NotFoundException.of(Request.class, requestId));

        request.setStatus(RequestStatus.CANCELED);
        Request updated = requestRepository.save(request);
        return RequestMapper.toDto(updated);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Getting requests for event id={} by initiator id={}", eventId, userId);
        checkUserExists(userId);
        Event event = getEventOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw ValidateException.of(ErrorMessages.NOT_EVENT_INITIATOR, userId, eventId);
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return RequestMapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestsStatus(Long userId, Long eventId,
                                                                    EventRequestStatusUpdateRequest updateRequest) {
        log.info("Updating request statuses for event id={} by initiator id={}", eventId, userId);
        checkUserExists(userId);
        Event event = getEventOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw ValidateException.of(ErrorMessages.NOT_EVENT_INITIATOR, userId, eventId);
        }

        List<ParticipationRequestDto> confirmedList = new ArrayList<>();
        List<ParticipationRequestDto> rejectedList = new ArrayList<>();

        if (!Boolean.TRUE.equals(event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            return new EventRequestStatusUpdateResult(confirmedList, rejectedList);
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (StatusForUpdateRequest.CONFIRMED.equals(updateRequest.getStatus())
                && event.getParticipantLimit() > 0
                && confirmedCount >= event.getParticipantLimit()) {
            throw ValidateException.of(ErrorMessages.PARTICIPANT_LIMIT_REACHED);
        }

        List<Request> requests = requestRepository.findAllByEventIdAndIdIn(eventId, updateRequest.getRequestIds());

        for (Request request : requests) {
            if (!RequestStatus.PENDING.equals(request.getStatus())) {
                throw ValidateException.of(ErrorMessages.REQUEST_NOT_PENDING);
            }

            if (StatusForUpdateRequest.CONFIRMED.equals(updateRequest.getStatus())) {
                if (confirmedCount < event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedCount++;
                    confirmedList.add(RequestMapper.toDto(request));
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedList.add(RequestMapper.toDto(request));
                }
            } else if (StatusForUpdateRequest.REJECTED.equals(updateRequest.getStatus())) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedList.add(RequestMapper.toDto(request));
            }
        }

        requestRepository.saveAll(requests);

        if (confirmedCount >= event.getParticipantLimit()) {
            List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
            for (Request r : pendingRequests) {
                r.setStatus(RequestStatus.REJECTED);
                rejectedList.add(RequestMapper.toDto(r));
            }
            requestRepository.saveAll(pendingRequests);
        }

        return new EventRequestStatusUpdateResult(confirmedList, rejectedList);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of(User.class, userId));
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw NotFoundException.of(User.class, userId);
        }
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> NotFoundException.of(Event.class, eventId));
    }
}