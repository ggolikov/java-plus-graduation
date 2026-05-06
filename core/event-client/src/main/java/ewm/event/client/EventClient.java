package ewm.event.client;

import ewm.event.client.dto.EventInternalDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@FeignClient(
        contextId = "eventFeign",
        name = "event-service",
        url = "${event.service.url:http://localhost:8080}"
)
public interface EventClient {

    @GetMapping("/internal/events/{id}")
    EventInternalDto getById(@PathVariable("id") Long eventId);

    @PostMapping("/internal/events")
    EventInternalDto save(@RequestBody EventInternalDto event);

    @GetMapping("/internal/events/by-initiator/{initiatorId}")
    List<EventInternalDto> getByInitiator(
            @PathVariable("initiatorId") Long initiatorId,
            @RequestParam("from") Integer from,
            @RequestParam("size") Integer size
    );

    @GetMapping("/internal/events/by-ids")
    List<EventInternalDto> getAllByIds(@RequestParam("ids") Set<Long> ids);

    @GetMapping("/internal/events/exists-by-category/{categoryId}")
    boolean existsByCategoryId(@PathVariable("categoryId") Long categoryId);

    default Optional<EventInternalDto> findById(Long eventId) {
        try {
            return Optional.ofNullable(getById(eventId));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    default List<EventInternalDto> findByInitiatorId(Long initiatorId, Pageable page) {
        int from = page == null ? 0 : page.getPageNumber() * page.getPageSize();
        int size = page == null ? 10 : page.getPageSize();
        List<EventInternalDto> events = getByInitiator(initiatorId, from, size);
        return events == null ? Collections.emptyList() : events;
    }

    default List<EventInternalDto> findAllByIdIn(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<EventInternalDto> events = getAllByIds(ids);
        return events == null ? Collections.emptyList() : events;
    }
}
