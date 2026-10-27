package ru.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.entity.Event;
import ru.practicum.event.entity.State;
import ru.practicum.request.entity.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    // для админа
    @Query("SELECT e FROM Event AS e WHERE " +
            "(:users IS NULL OR e.initiator.id IN :users) AND " +
            "(:states IS NULL OR e.state IN :states) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(:rangeStart IS NULL OR e.eventDate >= :rangeStart) AND " +
            "(:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    List<Event> findEventByParam(List<Long> users, List<State> states, List<Long> categories, LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd, Pageable pageable);

    //для публичного
    @Query("SELECT e FROM Event AS e WHERE e.state = :state AND " +
            "(:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(:paid IS NULL OR e.paid = :paid) AND " +
            "(:rangeStart IS NULL OR e.eventDate >= :rangeStart) AND " +
            "(:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) AND " +
            "(:onlyAvailable = false OR e.participantLimit = 0 OR " +
            "e.participantLimit > (SELECT COUNT(r) FROM Request AS r " +
            "WHERE r.event.id = e.id AND r.status = :confirmedStatus)) " +
            "ORDER BY e.eventDate")
    List<Event> findPublicEventByParam(State state, String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                       LocalDateTime rangeEnd, Boolean onlyAvailable, RequestStatus confirmedStatus, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);
}
