package ewm.event.mapper;

import ewm.common.dto.LocationDto;
import ewm.common.dto.event.EventFullDto;
import ewm.common.dto.event.EventShortDto;
import ewm.common.dto.user.UserDto;
import ewm.common.model.Location;
import ewm.event.dto.NewEventDto;
import ewm.event.dto.UpdateEventAdminRequest;
import ewm.event.dto.UpdateEventUserRequest;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.model.EventStateAction;

public class EventMapper {

    public static Event mapToEvent(UserDto initiator,
                                   NewEventDto eventDto,
                                   Long categoryId) {
        Event event = new Event();
        event.setInitiatorId(initiator.getId());
        event.setTitle(eventDto.getTitle());
        event.setAnnotation(eventDto.getAnnotation());
        event.setDescription(eventDto.getDescription());
        event.setCategoryId(categoryId);
        event.setEventDate(eventDto.getEventDate());
        Location location = new Location();
        location.setLat(eventDto.getLocation().getLat());
        location.setLon(eventDto.getLocation().getLon());
        event.setLocation(location);
        event.setPaid(eventDto.getPaid() != null ? eventDto.getPaid() : false);
        event.setParticipantLimit(eventDto.getParticipantLimit() != null ? eventDto.getParticipantLimit() : 0);
        event.setRequestModeration(eventDto.getRequestModeration() != null ? eventDto.getRequestModeration() : true);
        return event;
    }

    public static EventFullDto mapToEventFullDto(Event event, Double rating, Long confirmedRequests) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(event.getId());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setCategory(event.getCategoryId());
        eventFullDto.setCreatedOn(event.getCreatedOn());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setPublishedOn(event.getPublishedOn());
        eventFullDto.setInitiator(event.getInitiatorId());
        LocationDto locationDto = new LocationDto();
        locationDto.setLat(event.getLocation().getLat());
        locationDto.setLon(event.getLocation().getLon());
        eventFullDto.setLocation(locationDto);
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setState(event.getState() == null ? null : event.getState().name());
        eventFullDto.setRating(rating);

        // TODO Request
        eventFullDto.setConfirmedRequests(confirmedRequests);

        return eventFullDto;
    }

    public static EventShortDto mapToEventShortDto(Event event, Double rating, Long confirmedRequests) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(event.getId());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setCategory(event.getCategoryId());

        // TODO Request
        eventShortDto.setConfirmedRequests(confirmedRequests);
        eventShortDto.setRating(rating);
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setInitiator(event.getInitiatorId());
        eventShortDto.setPaid(event.getPaid());
        return eventShortDto;
    }

    public static Event updateEvent(Event event, UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.hasTitle()) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        if (updateEventUserRequest.hasAnnotation()) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.hasDescription()) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.hasEventDate()) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.hasLocation()) {
            Location location = new Location();
            location.setLat(updateEventUserRequest.getLocation().getLat());
            location.setLon(updateEventUserRequest.getLocation().getLon());
            event.setLocation(location);
        }
        if (updateEventUserRequest.hasPaid()) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.hasParticipantLimit()) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.hasRequestModeration()) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.hasStateAction()) {
            EventState eventState = updateEventUserRequest.getStateAction() == EventStateAction.SEND_TO_REVIEW
                    ? EventState.PENDING : EventState.CANCELED;
            event.setState(eventState);
        }
        return event;
    }

    public static Event updateEvent(Event event, UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest.hasTitle()) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        if (updateEventAdminRequest.hasAnnotation()) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.hasDescription()) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.hasEventDate()) {
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }

        if (updateEventAdminRequest.hasLocation()) {
            Location location = new Location();
            location.setLat(updateEventAdminRequest.getLocation().getLat());
            location.setLon(updateEventAdminRequest.getLocation().getLon());
            event.setLocation(location);
        }
        if (updateEventAdminRequest.hasPaid()) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.hasParticipantLimit()) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.hasRequestModeration()) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        return event;
    }
}
