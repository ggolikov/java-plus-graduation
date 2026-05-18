package ewm.event.controller;

import ewm.common.dto.event.EventFullDto;
import ewm.common.dto.event.EventShortDto;
import ewm.event.model.EventSort;
import ewm.event.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPublicController {
    private final EventService eventService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getPublicEvent(@RequestHeader("X-EWM-USER-ID") Long userId, @PathVariable("id") Long eventId, HttpServletRequest request) {
        return eventService.getPublicEvent(userId, eventId, request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getPublicEvents(@RequestParam(required = false) String text,
                                               @RequestParam(required = false) List<Long> categories,
                                               @RequestParam(required = false) Boolean paid,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                               @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                               @RequestParam(required = false) EventSort sort,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @PositiveOrZero int size,
                                               HttpServletRequest request) {
        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from,
                size, request);
    }

    public Iterator<RecommendedEventProto> getRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId, @RequestParam(required = false) Long maxSize) {
        return eventService.getRecommendations(userId, maxSize);
    }
}
