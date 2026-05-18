package ru.practicum.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.analyzer.model.EventCountProjection;
import ru.practicum.stats.analyzer.model.Interaction;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    @Query(value = """
        SELECT
            i.event_id AS eventId,
            COALESCE(SUM(i.rating), 0) AS count
                FROM interactions i
                WHERE i.event_id IN (:eventIds)
                GROUP BY i.event_id
            """, nativeQuery = true)
    List<EventCountProjection> countRatingsByEventIds(
            @Param("eventIds") List<Long> eventIds
    );
   @Query("SELECT i FROM Interaction i WHERE i.userId = :userId ORDER BY i.rating DESC LIMIT :maxResults")
   List<Interaction> findUserActions(Long userId, Long maxResults);

   @Query(value = """
        SELECT DISTINCT event_id
              FROM interactions
              WHERE event_id NOT IN (
                  SELECT event_id
                  FROM interactions
                  WHERE user_id = :userId
              ) ORDER BY rating DESC;
            """, nativeQuery = true)
    List<Interaction> findUserNewActions(Long userId);

    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);

    List<Interaction> findAllByEventIdIn(List<Long> collect);

    @Query(value = """
        SELECT rating FROM interactions WHERE user_id = :userId AND event_id = :eventId;
            """, nativeQuery = true)
    Long getEventRating(Long userId, Long eventId);
}
