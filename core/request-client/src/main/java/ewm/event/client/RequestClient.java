package ewm.event.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@FeignClient(
        contextId = "requestFeign",
        name = "request-service",
        url = "${request.service.url:http://localhost:8080}"
)
public interface RequestClient {

    @GetMapping("/internal/requests/confirmed-count")
    List<EventConfirmedCountDto> getConfirmedCountByEventIds(@RequestParam("eventIds") List<Long> eventIds);

    default List<EventConfirmedCountDto> countConfirmedByEventIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<EventConfirmedCountDto> rows = getConfirmedCountByEventIds(eventIds);
        return rows == null ? Collections.emptyList() : rows;
    }

    @Data
    class EventConfirmedCountDto {
        private Long eventId;
        private Long cnt;
    }
}
