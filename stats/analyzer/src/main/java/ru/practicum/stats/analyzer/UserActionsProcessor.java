package ru.practicum.stats.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.client.KafkaClient;
import ru.practicum.stats.analyzer.client.KafkaClientImplementation;
import ru.practicum.stats.analyzer.client.KafkaConsumerProperties;
import ru.practicum.stats.analyzer.service.UserActionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@EnableConfigurationProperties(KafkaConsumerProperties.class)
public class UserActionsProcessor {
    private final KafkaConsumerProperties kafkaConsumerProperties;
    private final UserActionService userActionService;
    private final KafkaClient kafkaClient;
    private final Consumer<Long, UserActionAvro> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public UserActionsProcessor(KafkaConsumerProperties kafkaConsumerProperties, UserActionService userActionService) {
        this.kafkaConsumerProperties = kafkaConsumerProperties;
        this.kafkaClient = new KafkaClientImplementation();
        this.userActionService = userActionService;
        this.consumer = kafkaClient.getUserActionsConsumer();
        consumer.subscribe(List.of(kafkaConsumerProperties.getActionsTopic()));
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    public void start() {
        try {
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(kafkaConsumerProperties.getRequestTimeout());
                if (!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<Long, UserActionAvro> record : records) {
                        UserActionAvro userActionAvro = record.value();
                        log.info("User event received: {}", record.value());

                        userActionService.processEvent(userActionAvro);
                        manageOffsets(record, count++);
                    }
                    consumer.commitAsync();
                }
            }

        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий", e);
        } finally {
            try {
                kafkaClient.getUserActionsConsumer().commitSync();

            } finally {
                log.info("Закрываем консьюмер");
                kafkaClient.getUserActionsConsumer().close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<?, ?> record, int count) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if(count % 100 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if(exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}