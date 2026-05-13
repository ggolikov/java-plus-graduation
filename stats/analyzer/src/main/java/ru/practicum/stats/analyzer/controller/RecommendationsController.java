package ru.practicum.stats.analyzer.controller;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.stats.analyzer.service.EventSimilarityService;
import ru.practicum.stats.analyzer.service.UserActionService;

@GrpcService
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final UserActionService userActionService;
    private final EventSimilarityService eventSimilarityService;

    public RecommendationsController(UserActionService userActionService, EventSimilarityService eventSimilarityService) {
        this.userActionService = userActionService;
        this.eventSimilarityService = eventSimilarityService;
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        userActionService.getUserPredictions(request);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        eventSimilarityService.getSimilarEvents(request);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        userActionService.getInteractionsCount(request);
    }
}
