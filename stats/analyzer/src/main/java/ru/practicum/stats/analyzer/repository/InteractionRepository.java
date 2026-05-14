package ru.practicum.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats.analyzer.model.Interaction;

import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    @Query("SELECT sum(i.rating) FROM Interaction i WHERE i.eventId = :eventId")
    Long countRatingByEventId(long eventId);

   @Query("SELECT i FROM Interaction i WHERE i.userId = :userId ORDER BY i.rating DESC")
    List<Interaction> findUserActions(Long userId);

    Boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
