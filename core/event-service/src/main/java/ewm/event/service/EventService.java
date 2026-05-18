package ewm.event.service;

import ewm.event.dto.*;
import ewm.common.dto.event.*;
import ewm.event.model.EventSort;
import ewm.event.model.EventState;
import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

public interface EventService {
    EventFullDto create(Long userId, NewEventDto eventDto);

    EventFullDto get(Long userId, Long eventId);

    List<EventFullDto> get(List<Long> users,
                           List<EventState> states,
                           List<Long> categories,
                           LocalDateTime rangeStart,
                           LocalDateTime rangeEnd,
                           int from,
                           int size);

    EventFullDto getPublicEvent(Long userId, Long eventId, HttpServletRequest request);

    List<EventShortDto> getEvents(Long userId, int from, int size);

    List<EventShortDto> getPublicEvents(String text,
                                        List<Long> categories,
                                        Boolean paid,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Boolean onlyAvailable,
                                        EventSort sort,
                                        int from,
                                        int size,
                                        HttpServletRequest request);

    EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    Iterator<RecommendedEventProto> getRecommendations(Long userId, Long maxSize);

    void likeEvent(Long userId, Long eventId);
}
