package client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(
        name = "stats-server",
        url = "${stats.service.url:http://localhost:9090}"
)
@Qualifier("statsFeign")
public interface StatsClient {

    @PostMapping("/hit")
    void hit(@RequestBody EndpointHitDto endpointHit);

    @GetMapping("/stats")
    List<ViewStatsDto> getStats(
            @RequestParam("start")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime start,
            @RequestParam("end")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime end,
            @RequestParam(value = "uris", required = false)
            List<String> uris,
            @RequestParam(value = "unique", required = false)
            Boolean unique
    );
}
