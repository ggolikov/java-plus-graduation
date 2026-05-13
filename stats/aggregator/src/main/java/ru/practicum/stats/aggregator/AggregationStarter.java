package ru.practicum.stats.aggregator;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.client.KafkaClient;
import ru.practicum.stats.aggregator.client.KafkaClientImplementation;
import ru.practicum.stats.aggregator.client.KafkaConsumerProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
@EnableConfigurationProperties(KafkaConsumerProperties.class)
public class    AggregationStarter {
    private final KafkaConsumerProperties kafkaConsumerProperties;
    private KafkaClient kafkaClient;
    private Aggregator aggregator;

    public AggregationStarter(KafkaConsumerProperties kafkaConsumerProperties) {
        this.kafkaConsumerProperties = kafkaConsumerProperties;
        this.kafkaClient = new KafkaClientImplementation();
        this.aggregator = new Aggregator();
    }

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */
    public void start() {
        try {
            Consumer<String, UserActionAvro> consumer = kafkaClient.getConsumer();
            Producer<String, EventSimilarityAvro> producer = kafkaClient.getProducer();
            consumer.subscribe(List.of(kafkaConsumerProperties.getIncomingTopic()));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    List<EventSimilarityAvro> similarities = aggregator.updateEventSimilarities(record.value());

                    for (EventSimilarityAvro eventSimilarityAvro : similarities) {
                        String similaritiesTopic = kafkaConsumerProperties.getOutgoingTopic();

                        ProducerRecord<String, EventSimilarityAvro> similarityRecord = new ProducerRecord<>(
                                similaritiesTopic,
                                null,
                                eventSimilarityAvro.getTimestamp().toEpochMilli(),
                                // TODO: заменить на id
                                eventSimilarityAvro.getTimestamp().toString(),
                                eventSimilarityAvro
                        );
                        producer.send(similarityRecord);
                    }

                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                // Перед тем, как закрыть продюсер и консьюмер, нужно убедиться,
                // что все сообщения, лежащие в буффере, отправлены и
                // все оффсеты обработанных сообщений зафиксированы

                // здесь нужно вызвать метод продюсера для сброса данных в буффере
                kafkaClient.getProducer().flush();
                // здесь нужно вызвать метод консьюмера для фиксации смещений
                kafkaClient.getConsumer().commitSync();

            } finally {
                log.info("Закрываем консьюмер");
                kafkaClient.getConsumer().close();
                log.info("Закрываем продюсер");
                kafkaClient.getProducer().close();
            }
        }
    }
}