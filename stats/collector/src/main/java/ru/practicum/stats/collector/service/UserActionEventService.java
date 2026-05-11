package ru.practicum.stats.collector.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.collector.client.KafkaClient;
import ru.practicum.ewm.stats.proto.*;

import java.time.Instant;

@Service
public class UserActionEventService {
    @Value("${kafka.topic.hubs:stats.user-actions.v1}")
    private String userActionsTopic;

    private final KafkaClient kafkaClient;

    public UserActionEventService(KafkaClient kafkaClient) {
        this.kafkaClient = kafkaClient;
    }

    public void collectUserActionEvent(UserActionProto event, StreamObserver<Empty> responseObserver) {
        try {
            UserActionAvro userActionEventAvro = convertToAvro(event);
            Long timestamp = Instant.now().toEpochMilli();

            ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                    userActionsTopic,
                    null,
                    timestamp,
                    event.getEventId(),
                    userActionEventAvro
            );
            kafkaClient.getProducer().send(record);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private UserActionAvro convertToAvro(UserActionProto event) {
        Instant timestamp = Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos());

        return UserActionAvro.newBuilder()
            .setUserId(event.getUserId())
                .setEventId(event.getEventId())
                .setActionType(ActionTypeAvro.valueOf(event.getActionType().name()))
                .setTimestamp(timestamp)
            .build();
    }
}
