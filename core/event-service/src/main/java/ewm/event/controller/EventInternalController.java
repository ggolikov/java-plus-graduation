package ewm.event.controller;

import ewm.event.client.EventInternalMapper;
import ewm.event.client.dto.EventInternalDto;
import ewm.event.model.Event;
import ewm.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class EventInternalController {
    private final EventRepository eventRepository;

    @GetMapping("/{id}")
    public EventInternalDto getById(@PathVariable("id") Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        return event == null ? null : EventInternalMapper.toDto(event);
    }

    @PostMapping
    public EventInternalDto save(@RequestBody EventInternalDto dto) {
        Event saved = eventRepository.save(EventInternalMapper.toEntity(dto));
        return EventInternalMapper.toDto(saved);
    }

    @GetMapping("/by-initiator/{initiatorId}")
    public List<EventInternalDto> getByInitiator(@PathVariable Long initiatorId,
                                                 @RequestParam Integer from,
                                                 @RequestParam Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return eventRepository.findByInitiatorId(initiatorId, page).stream()
                .map(EventInternalMapper::toDto)
                .toList();
    }

    @GetMapping("/by-ids")
    public List<EventInternalDto> getAllByIds(@RequestParam Set<Long> ids) {
        return eventRepository.findAllByIdIn(ids).stream()
                .map(EventInternalMapper::toDto)
                .toList();
    }

    @GetMapping("/exists-by-category/{categoryId}")
    public boolean existsByCategoryId(@PathVariable Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }
}
