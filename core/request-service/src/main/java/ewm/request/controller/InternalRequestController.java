package ewm.request.controller;

import ewm.event.client.RequestClient;
import ewm.request.repository.ParticipationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/requests")
public class InternalRequestController {

    private final ParticipationRequestRepository requestRepository;

    @GetMapping("/confirmed-count")
    public List<RequestClient.EventConfirmedCountDto> getConfirmedCountByEventIds(
            @RequestParam("eventIds") List<Long> eventIds
    ) {
        return requestRepository.countConfirmedByEventIds(eventIds).stream()
                .map(row -> {
                    RequestClient.EventConfirmedCountDto dto = new RequestClient.EventConfirmedCountDto();
                    dto.setEventId(row.getEventId());
                    dto.setCnt(row.getCnt());
                    return dto;
                })
                .toList();
    }
}
