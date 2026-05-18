package ru.practicum.ewm.client.stats;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.*;

import java.util.Iterator;

@Component
public class RecommendationsClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public Iterator<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        return client.getRecommendationsForUser(request);
    }

    public Iterator<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        return client.getSimilarEvents(request);
    }

    public Iterator<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        return client.getInteractionsCount(request);
    }
}
