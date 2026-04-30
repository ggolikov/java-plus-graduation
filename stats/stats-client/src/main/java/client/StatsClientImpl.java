package client;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StatsClientImpl implements StatsClient {

    private final StatsClient statsFeignClient;

    public StatsClientImpl(StatsClient statsFeignClient) {
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
        return statsFeignClient.getStats(start, end, uris, unique);
    }
}
