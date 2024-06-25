package otter.jet.reader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import otter.jet.store.MessageStore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableConfigurationProperties(ReaderConfigurationProperties.class)
class ReaderConfiguration {

    private final ReaderConfigurationProperties readerConfigurationProperties;

    ReaderConfiguration(ReaderConfigurationProperties readerConfigurationProperties) {
        this.readerConfigurationProperties = readerConfigurationProperties;
    }

    @Bean
    public ReaderService readerService(
            @Value("${nats.server.host}") String natsServerHost,
            @Value("${nats.server.port}") String natsServerPort,
            @Value("${read.beginTimestamp:}") String startDate,
            MessageDeserializer messageDeserializer,
            MessageStore messageStore) {
        return new ReaderService(
                createNatsServerUrl(natsServerHost, natsServerPort),
                messageDeserializer,
                readerConfigurationProperties.getSubject(),
                messageStore,
                resolveBeginTimestamp(startDate));
    }

    private static LocalDateTime resolveBeginTimestamp(String startDate) {
        if (!startDate.isBlank()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(startDate, formatter);
        } else {
            return LocalDateTime.MIN;
        }
    }

    private String createNatsServerUrl(String natsServerHost, String natsServerPort) {
        return "nats://" + natsServerHost + ":" + natsServerPort;
    }
}
