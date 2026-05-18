package ru.practicum.stats.analyzer.controller;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.stats.analyzer.model.EventCountProjection;
import ru.practicum.stats.analyzer.model.Similarity;
import ru.practicum.stats.analyzer.service.EventSimilarityService;
import ru.practicum.stats.analyzer.service.UserActionService;

import java.util.List;

@Slf4j
@GrpcService
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final UserActionService userActionService;
    private final EventSimilarityService eventSimilarityService;

    public RecommendationsController(UserActionService userActionService, EventSimilarityService eventSimilarityService) {
        this.userActionService = userActionService;
        this.eventSimilarityService = eventSimilarityService;
    }

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            List<RecommendedEventProto> recommendations =
                    userActionService.getUserPredictions(request);

            for (RecommendedEventProto item : recommendations) {
                responseObserver.onNext(item);
            }

            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in getRecommendationsForUser", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            List<Similarity> similarEvents =
                    eventSimilarityService.getSimilarEvents(request);

            for (Similarity item : similarEvents) {
                RecommendedEventProto response =
                        RecommendedEventProto.newBuilder()
                                .setEventId(item.getEvent2())
                                .setScore(item.getSimilarity())
                                .build();

                responseObserver.onNext(response);
            }

            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in getSimilarEvents", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            List<EventCountProjection> counts =
                    userActionService.getInteractionsCount(request);

            for (EventCountProjection item : counts) {
                RecommendedEventProto response =
                        RecommendedEventProto.newBuilder()
                                .setEventId(item.getEventId())
                                .setScore(item.getCount())
                                .build();

                responseObserver.onNext(response);
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getInteractionsCount", e);
            responseObserver.onError(e);
        }
    }
}
