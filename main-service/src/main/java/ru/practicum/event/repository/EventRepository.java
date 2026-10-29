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
            "(e.initiator.id IN :users) AND " +
            "(e.state IN :states) AND " +
            "(e.category.id IN :categories) AND " +
            "(e.eventDate >= :rangeStart) AND " +
            "(e.eventDate <= :rangeEnd) " +
            "ORDER BY e.id")
    List<Event> findEventByParam(List<Long> users, List<State> states, List<Long> categories, LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd, Pageable pageable);

    //для публичного
    @Query("SELECT e FROM Event AS e WHERE e.state = :state AND " +
            "(:text = '%%' OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) AND " +
            "(e.category.id IN :categories) AND " +
            "(e.paid IN :paidValues) AND " +
            "(e.eventDate >= :rangeStart) AND " +
            "(e.eventDate <= :rangeEnd) AND " +
            "(:onlyAvailable = false OR e.participantLimit = 0 OR " +
            "e.participantLimit > (SELECT COUNT(r) FROM Request AS r " +
            "WHERE r.event.id = e.id AND r.status = :confirmedStatus)) " +
            "ORDER BY e.eventDate")
    List<Event> findPublicEventByParam(State state, String text, List<Long> categories, List<Boolean> paidValues, LocalDateTime rangeStart,
                                       LocalDateTime rangeEnd, Boolean onlyAvailable, RequestStatus confirmedStatus, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);
}
