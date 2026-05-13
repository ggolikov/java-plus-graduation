package ru.practicum.stats.analyzer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "similarities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event1", "event2"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Similarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event1", nullable = false)
    private Long event1;

    @Column(name = "event2", nullable = false)
    private Long event2;

    @Column(nullable = false)
    private Double similarity;

    @Column(name = "ts", nullable = false)
    private OffsetDateTime ts;
}
