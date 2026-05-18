package ru.practicum.stats.analyzer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "interactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Double rating;

    @Column(name = "ts", nullable = false)
    private OffsetDateTime ts;
}
