package otter.jet.reader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import otter.jet.store.MessageStore;

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
            MessageDeserializer messageDeserializer,
            MessageStore messageStore) {
        return new ReaderService(
                createNatsServerUrl(natsServerHost, natsServerPort),
                messageDeserializer,
                readerConfigurationProperties.getSubject(),
                messageStore);
    }

    private String createNatsServerUrl(String natsServerHost, String natsServerPort) {
        return "nats://" + natsServerHost + ":" + natsServerPort;
    }
}
