package ru.practicum.compilations.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.event.entity.Event;

import java.util.List;

@Entity
@Table(name = "compilations")
@Data
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Создаем потому что у подборки может быть много собитий, как и наборот
    // связующая таблица с двумя FK ключами на таблицу эту и таблицу с событиями
    @ManyToMany
    @JoinTable(name = "compilations_events",
    joinColumns = @JoinColumn(name = "compilation_id"),
    inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> events;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "is_pinned")
    private Boolean pinned;

}
