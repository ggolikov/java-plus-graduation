package ewm.event.client;

import ewm.category.model.Category;
import ewm.common.model.Location;
import ewm.event.client.dto.EventInternalDto;
import ewm.event.model.Event;
import ewm.event.model.EventState;

public final class EventInternalMapper {
    private EventInternalMapper() {
    }

    public static EventInternalDto toDto(Event event) {
        EventInternalDto dto = new EventInternalDto();
        dto.setId(event.getId());
        dto.setInitiatorId(event.getInitiatorId());
        dto.setCategoryId(event.getCategoryId() == null ? null : event.getCategoryId());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setTitle(event.getTitle());
        dto.setLat(event.getLocation() == null ? null : event.getLocation().getLat());
        dto.setLon(event.getLocation() == null ? null : event.getLocation().getLon());
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setEventDate(event.getEventDate());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setState(event.getState() == null ? null : event.getState().name());
        return dto;
    }

    public static Event toEntity(EventInternalDto dto) {
        Event event = new Event();
        event.setId(dto.getId());
        event.setInitiatorId(dto.getInitiatorId());
        if (dto.getCategoryId() != null) {
            event.setCategoryId(dto.getCategoryId());
        }
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setTitle(dto.getTitle());
        if (dto.getLat() != null && dto.getLon() != null) {
            Location location = new Location();
            location.setLat(dto.getLat());
            location.setLon(dto.getLon());
            event.setLocation(location);
        }
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setConfirmedRequests(dto.getConfirmedRequests());
        event.setEventDate(dto.getEventDate());
        event.setCreatedOn(dto.getCreatedOn());
        event.setPublishedOn(dto.getPublishedOn());
        if (dto.getState() != null) {
            event.setState(EventState.valueOf(dto.getState()));
        }
        return event;
    }
}
