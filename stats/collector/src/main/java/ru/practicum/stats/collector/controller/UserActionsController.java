package ru.practicum.stats.collector.controller;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.stats.collector.service.UserActionEventService;

@GrpcService
public class UserActionsController extends UserActionControllerGrpc.UserActionControllerImplBase  {
    private final UserActionEventService userActionEventService;

    public UserActionsController(UserActionEventService userActionEventService) {
        this.userActionEventService = userActionEventService;
    }

    @Override
    public void collectUserAction(UserActionProto event, StreamObserver<Empty> responseObserver) {
        userActionEventService.collectUserActionEvent(event, responseObserver);
    }
}
