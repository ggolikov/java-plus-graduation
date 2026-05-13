package ru.practicum.ewm.client.stats;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

/**
 * gRPC client for the collector {@code UserActionController} service ({@code CollectUserAction}).
 */
@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void collectUserAction(UserActionProto event) {
        client.collectUserAction(event);
    }
}
