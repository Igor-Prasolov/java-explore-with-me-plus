package ru.practicum.hit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.hit.entity.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT new ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT(h)) FROM Hit AS h WHERE h.timestamp BETWEEN ?1 AND ?2" +
            " AND (?3 IS NULL OR h.uri in ?3) GROUP BY h.app, h.uri ORDER BY COUNT(h) DESC")
    List<ViewStatsDto> findStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT(DISTINCT h.ip)) FROM Hit AS h WHERE h.timestamp BETWEEN ?1 AND ?2" +
            " AND (?3 IS NULL OR h.uri in ?3) GROUP BY h.app, h.uri ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStatsDto> findUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);


}
