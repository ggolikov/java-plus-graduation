package ewm.request.service;

import ewm.common.exception.ConflictException;
import ewm.common.exception.NotFoundException;
import ewm.event.client.EventClient;
import ewm.event.client.dto.EventInternalDto;
import ewm.request.dto.EventRequestStatusUpdateRequest;
import ewm.request.dto.EventRequestStatusUpdateResult;
import ewm.request.dto.ParticipationRequestDto;
import ewm.request.mapper.ParticipationRequestMapper;
import ewm.request.model.ParticipationRequest;
import ewm.request.model.RequestStatus;
import ewm.request.repository.ParticipationRequestRepository;
import ewm.user.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepo;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        EventInternalDto event = eventClient.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (Objects.equals(event.getInitiatorId(), userId)) {
            throw new ConflictException("Initiator cannot request own event");
        }
        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Event not published");
        }
        if (requestRepo.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Duplicate request");
        }

        long limit = event.getParticipantLimit();
        long confirmed = event.getConfirmedRequests();

        if (limit > 0 && confirmed >= limit) {
            throw new ConflictException("Participant limit reached");
        }

        ParticipationRequest req = new ParticipationRequest();
        req.setRequesterId(userId);
        req.setEventId(eventId);

//        boolean moderation = event.getRequestModeration();
//        if (!moderation && (limit == 0 || confirmed < limit)) {
//            req.setStatus(RequestStatus.CONFIRMED);
//            event.setConfirmedRequests(confirmed + 1);
//        } else {
//            req.setStatus(RequestStatus.PENDING);
//        }

        RequestStatus status;
        if (event.getParticipantLimit() == 0 || Boolean.FALSE.equals(event.getRequestModeration())) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = RequestStatus.PENDING;
        }
        req.setStatus(status);

        // если автоподтверждение и лимит > 0 — повторная защита от гонки
        if (status == RequestStatus.CONFIRMED && limit > 0) {
            long confirmedAfter = requestRepo.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedAfter >= limit) {
                throw new ConflictException("Participant limit reached");
            }
        }

        ParticipationRequest saved = requestRepo.save(req);
        return ParticipationRequestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        ParticipationRequest req = requestRepo.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));

        if (req.getStatus() == RequestStatus.CONFIRMED) {
            // event confirmed counter is managed in event-service
        }

        req.setStatus(RequestStatus.CANCELED);
        return ParticipationRequestMapper.toDto(req);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        ensureUserExists(userId);
        return requestRepo.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        EventInternalDto event = eventClient.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new ConflictException("Only initiator can view event requests");
        }
        return requestRepo.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(
            Long userId,
            Long eventId,
            List<Long> requestIds,
            EventRequestStatusUpdateRequest.RequestUpdateStatus status
    ) {
        EventInternalDto event = eventClient.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new ConflictException("Only initiator can update requests");
        }

        List<ParticipationRequest> requests =
                requestRepo.findAllByIdInAndEventId(requestIds, eventId);

        if (requests.size() != new HashSet<>(requestIds).size()) {
            throw new NotFoundException("Some requests not found for event");
        }

        for (ParticipationRequest r : requests) {
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Only PENDING requests can be updated");
            }
        }
        List<ParticipationRequestDto> eventRequests =  getEventRequests(userId, eventId);

        List<ParticipationRequest> confirmedOut = new ArrayList<>();
        List<ParticipationRequest> rejectedOut = new ArrayList<>();

        if (status == EventRequestStatusUpdateRequest.RequestUpdateStatus.REJECTED) {
            for (ParticipationRequest r : requests) {
                r.setStatus(RequestStatus.REJECTED);
                rejectedOut.add(r);
            }
            return toResult(confirmedOut, rejectedOut);
        }

        // status == CONFIRMED
        long limit = event.getParticipantLimit();
        long confirmed = eventRequests.stream().filter(r -> Objects.equals(r.getStatus(), "CONFIRMED")).count();

        if (limit > 0 && confirmed >= limit) {
            throw new ConflictException("Participant limit reached");
        }

        long slots = (limit == 0) ? Long.MAX_VALUE : (limit - confirmed);

        for (ParticipationRequest r : requests) {
            if (slots > 0) {
                r.setStatus(RequestStatus.CONFIRMED);
                confirmedOut.add(r);
                slots--;
                confirmed++;
            } else {
                r.setStatus(RequestStatus.REJECTED);
                rejectedOut.add(r);
            }
        }

        // event confirmed counter is managed in event-service
        return toResult(confirmedOut, rejectedOut);
    }

    private EventRequestStatusUpdateResult toResult(List<ParticipationRequest> confirmed,
                                                    List<ParticipationRequest> rejected) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream().map(ParticipationRequestMapper::toDto).toList())
                .rejectedRequests(rejected.stream().map(ParticipationRequestMapper::toDto).toList())
                .build();
    }

    private void ensureUserExists(Long userId) {
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
    }
}