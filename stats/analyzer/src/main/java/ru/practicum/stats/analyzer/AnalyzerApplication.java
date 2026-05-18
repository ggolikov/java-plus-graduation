package ru.practicum.stats.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(AnalyzerApplication.class, args);

        final EventSimilarityProcessor eventSimilarityProcessor =
                context.getBean(EventSimilarityProcessor.class);
        UserActionsProcessor userActionsProcessor =
                context.getBean(UserActionsProcessor.class);

        // запускаем в отдельном потоке обработчик событий от агрегатора
        Thread thread = new Thread(eventSimilarityProcessor);
        thread.setName("EventSimilaritiesThread");
        thread.start();

        // В текущем потоке начинаем обработку
        // пользовательских событий от коллектора
        userActionsProcessor.start();
    }
}