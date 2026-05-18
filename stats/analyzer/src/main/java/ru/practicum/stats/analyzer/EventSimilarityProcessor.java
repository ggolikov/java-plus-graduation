package ru.practicum.stats.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.client.KafkaClient;
import ru.practicum.stats.analyzer.client.KafkaClientImplementation;
import ru.practicum.stats.analyzer.client.KafkaConsumerProperties;
import ru.practicum.stats.analyzer.service.EventSimilarityService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@EnableConfigurationProperties(KafkaConsumerProperties.class)
public class EventSimilarityProcessor implements Runnable {
    private final KafkaConsumerProperties kafkaConsumerProperties;
    private final EventSimilarityService eventSimilarityService;
    private final KafkaClient kafkaClient;
    private final Consumer<String, EventSimilarityAvro> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public EventSimilarityProcessor(KafkaConsumerProperties kafkaConsumerProperties, KafkaClient kafkaClient,
                                    EventSimilarityService eventSimilarityService) {
        this.kafkaConsumerProperties = kafkaConsumerProperties;
        this.kafkaClient = new KafkaClientImplementation();
        this.eventSimilarityService = eventSimilarityService;
        consumer = kafkaClient.getEventSimilaritiesConsumer();
        consumer.subscribe(List.of(kafkaConsumerProperties.getSimilaritiesTopic()));
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    @Override
    public void run() {
        try {
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(kafkaConsumerProperties.getRequestTimeout());
                if (!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                        EventSimilarityAvro event = record.value();
                        if (event == null) {
                            continue;
                        }
                        log.info("Similarity event received: {}", event);
                        eventSimilarityService.processEvent(event);
                        manageOffsets(record, count++);
                    }
                    consumer.commitSync();
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий", e);
        } finally {
            try {
                if (consumer != null) {
                    // фиксируем смещения для обработанных сообщений
                    consumer.commitSync(currentOffsets);
                }
            } finally {
                if (consumer != null) {
                    log.info("Закрываем консьюмер");
                    consumer.close();
                }
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