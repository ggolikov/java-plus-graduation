package ewm.event.service;

import client.StatsClient;
import ewm.category.model.Category;
import ewm.category.repository.CategoryRepository;
import ewm.common.exception.BadRequestException;
import ewm.common.exception.ConflictException;
import ewm.common.exception.NotFoundException;
import ewm.event.dto.*;
import ewm.common.dto.event.*;
import ewm.event.client.RequestClient;
import ewm.event.client.dto.EventInternalDto;
import ewm.event.mapper.EventMapper;
import ewm.event.model.Event;
import ewm.event.model.EventSort;
import ewm.event.model.EventState;
import ewm.event.model.EventStateActionAdmin;
import ewm.event.repository.DatabaseEventSearchRepository;
import ewm.event.repository.EventRepository;
import ewm.user.client.UserClient;
import ewm.common.dto.user.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final UserClient userClient;
    private final EventRepository eventRepository;
    private final DatabaseEventSearchRepository  databaseEventSearchRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private final RequestClient requestClient;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto eventDto) {
        isEventTimeValid(eventDto.getEventDate());

        UserDto user = userClient.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Event event = EventMapper.mapToEvent(user, eventDto, eventDto.getCategory());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);

        List<Event> eventList = List.of(event);

        return this.mapToEventFullDto(eventList).getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto get(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event not found");
        }

        List<Event> eventList = List.of(event);

        return this.mapToEventFullDto(eventList).getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> get(List<Long> users,
                                  List<EventState> states,
                                  List<Long> categories,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  int from,
                                  int size) {
        Pageable page = PageRequest.of(from / size, size);

        List<Event> eventList = databaseEventSearchRepository.findForAdmin(users, states, categories, rangeStart, rangeEnd, page);

        return this.mapToEventFullDto(eventList);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event is not published");
        }

        List<Event> eventList = List.of(event, event);
        registerHit(request);
        return this.mapToEventFullDto(eventList).getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        userClient.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Pageable page = PageRequest.of(from / size, size);

        List<Event> eventList = eventRepository.findByInitiatorId(userId, page);

        return this.mapToEventShortDto(eventList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               EventSort sort,
                                               int from,
                                               int size,
                                               HttpServletRequest request) {

        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("Range start must be before rangeEnd");
        }

        // Pageable: сортировка только по eventDate, не по views
        Pageable page;
        if (sort == EventSort.EVENT_DATE) {
            page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        } else {
            page = PageRequest.of(from / size, size);
        }

         registerHit(request);

        List<Event> eventList = databaseEventSearchRepository.findPublicEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, page
        );

        List<EventShortDto> dtos = mapToEventShortDto(eventList);

        if (sort == EventSort.VIEWS) {
            dtos.sort(Comparator.comparingLong(EventShortDto::getViews).reversed());
        }

        return dtos;
    }


    @Override
    @Transactional
    public EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event currentEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (currentEvent.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Event is already published");
        }

        if (!currentEvent.getInitiatorId().equals(userId)) {
            throw new BadRequestException("User not allowed to update event");
        }

        Event updatedEvent = EventMapper.updateEvent(currentEvent, updateEventUserRequest);

        if (updateEventUserRequest.hasCategory() &&
                !updatedEvent.getCategoryId().equals(updateEventUserRequest.getCategory())) {
            updatedEvent.setCategoryId(updateEventUserRequest.getCategory());
        }

        isEventTimeValid(updatedEvent.getEventDate());
        updatedEvent = eventRepository.save(updatedEvent);

        List<Event> eventList = List.of(updatedEvent);

        return this.mapToEventFullDto(eventList).getFirst();
    }

    @Override
    @Transactional
    public EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event currentEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (currentEvent.getState().equals(EventState.PUBLISHED) &&
                updateEventAdminRequest.getEventDate() != null &&
                updateEventAdminRequest.getEventDate().isAfter(currentEvent.getPublishedOn().minusHours(1))) {
            throw new ConflictException("Invalid event time");
        }

        if (updateEventAdminRequest.getStateAction() != null) {
            if (currentEvent.getState().equals(EventState.PENDING)) {
                if (updateEventAdminRequest.getStateAction().equals(EventStateActionAdmin.PUBLISH_EVENT)) {
                    currentEvent.setState(EventState.PUBLISHED);
                    currentEvent.setPublishedOn(LocalDateTime.now());
                } else {
                    currentEvent.setState(EventState.CANCELED);
                }
            } else {
                throw new ConflictException("Invalid event state");
            }
        }

        Event updatedEvent = EventMapper.updateEvent(currentEvent, updateEventAdminRequest);

        if (updateEventAdminRequest.hasCategory() &&
                !updatedEvent.getCategoryId().equals(updateEventAdminRequest.getCategory())) {
            updatedEvent.setCategoryId(updateEventAdminRequest.getCategory());
        }

        updatedEvent = eventRepository.save(updatedEvent);

        List<Event> eventList = List.of(updatedEvent);

        return this.mapToEventFullDto(eventList).getFirst();
    }

    private void isEventTimeValid(LocalDateTime eventTime) {
        if (eventTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Invalid event time");
        }
    }

    private void registerHit(HttpServletRequest request) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("main-service");
        endpointHitDto.setUri(request.getRequestURI());
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now());
        statsClient.hit(endpointHitDto);
    }

    private Map<Long, Integer> getEventsViews(List<Event> eventList) {
        if (eventList == null || eventList.isEmpty()) return Map.of();

        List<String> uris = eventList.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        LocalDateTime start = eventList.stream()
                .map(Event::getCreatedOn)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(LocalDateTime.now().minusYears(1));

        LocalDateTime end = LocalDateTime.now();

        try {
            List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

            Map<Long, Integer> map = new HashMap<>();
            for (ViewStatsDto s : stats) {
                String[] parts = s.getUri().split("/");
                if (parts.length >= 3) {
                    long eventId = Long.parseLong(parts[2]);
                    map.put(eventId, (int) s.getHits());
                }
            }
            return map;
        } catch (Exception ex) {
            // критично: не роняем эндпоинт
            return Map.of();
        }
    }

    private List<EventFullDto> mapToEventFullDto(List<Event> eventList) {
        Map<Long, Integer> views = getEventsViews(eventList);
        Map<Long, Long> confirmed = getConfirmedRequests(eventList);

        return eventList.stream()
                .map(e -> EventMapper.mapToEventFullDto(
                        e,
                        views.getOrDefault(e.getId(), 0),
                        confirmed.getOrDefault(e.getId(), 0L)
                ))
                .toList();
    }


    private Map<Long, Long> getConfirmedRequests(List<Event> eventList) {
        if (eventList == null || eventList.isEmpty()) return Map.of();

        List<Long> ids = eventList.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> map = new HashMap<>();
        for (RequestClient.EventConfirmedCountDto row
                : requestClient.countConfirmedByEventIds(ids)) {
            map.put(row.getEventId(), row.getCnt());
        }
        return map;
    }


    private List<EventShortDto> mapToEventShortDto(List<Event> eventList) {
        Map<Long, Integer> views = getEventsViews(eventList);
        Map<Long, Long> confirmed = getConfirmedRequests(eventList);

        return eventList.stream()
                .map(e -> EventMapper.mapToEventShortDto(
                        e,
                        views.getOrDefault(e.getId(), 0),
                        confirmed.getOrDefault(e.getId(), 0L)
                ))
                .toList();
    }
}
