package ru.practicum.compilations.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.event.entity.Event;

import java.util.Set;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "compilation-with-events",
        attributeNodes = @NamedAttributeNode(value = "events", subgraph = "event"),
        subgraphs = @NamedSubgraph(name = "event", attributeNodes = {
                @NamedAttributeNode("category"),
                @NamedAttributeNode("initiator"),
        })
)
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Создаем потому что у подборки может быть много собитий, как и наборот
    // связующая таблица с двумя FK ключами на таблицу эту и таблицу с событиями
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "compilation_events",
    joinColumns = @JoinColumn(name = "compilation_id"),
    inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<Event> events;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "is_pinned")
    private Boolean pinned;

}
