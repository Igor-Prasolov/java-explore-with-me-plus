package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.request.entity.Request;
import ru.practicum.request.entity.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<Request> findAllByRequesterId(Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventIdAndIdIn(Long eventId, List<Long> requestIds);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long requesterId);

    @Query("SELECT r.event.id, COUNT(r.requester.id) FROM Request AS r " +
            "WHERE r.status = :status AND " +
            "r.event.id IN :eventIds " +
            "GROUP BY r.event.id")
    List<Object[]> countByEventIdsAndStatus(List<Long> eventIds, RequestStatus status);
}
