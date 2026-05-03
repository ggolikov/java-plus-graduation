package client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@Primary
public class StatsClientImpl implements StatsClient {

    private final StatsClient statsFeignClient;

    public StatsClientImpl(@Qualifier("statsFeign") StatsClient statsFeignClient) {
        this.statsFeignClient = statsFeignClient;
    }

    @Override
    public void hit(EndpointHitDto endpointHit) {
        statsFeignClient.hit(endpointHit);
    }

    @Override
    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique
    ) {
        List<ViewStatsDto> body = statsFeignClient.getStats(start, end, uris, unique);
        return body != null ? body : Collections.emptyList();
    }
}
