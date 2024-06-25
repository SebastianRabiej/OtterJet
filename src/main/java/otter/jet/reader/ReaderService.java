package otter.jet.reader;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Subscription;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import otter.jet.store.MessageStore;

public class ReaderService {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderService.class);
    private static final String NO_MATCHING_STREAM_CODE = "SUB-90007";

    private final String natsServerUrl;
    private final MessageDeserializer messageDeserializer;
    private final String subject;
    private final MessageStore messageStore;

    private final Executor executorService = Executors.newSingleThreadExecutor();

    public ReaderService(String natsServerUrl,
                         MessageDeserializer messageDeserializer,
                         String subject,
                         MessageStore messageStore) {
        this.natsServerUrl = natsServerUrl;
        this.messageDeserializer = messageDeserializer;
        this.subject = subject;
        this.messageStore = messageStore;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startReadingMessages() {
        // This method will be invoked after the service is initialized
        startMessageListener();
    }

    private void startMessageListener() {
        executorService.execute(
                () -> {
                    // Connect to NATS server
                    try (Connection natsConnection = Nats.connect(natsServerUrl)) {
                        LOG.info("Connected to NATS server at: {}", natsServerUrl);

                        JetStream jetStream = natsConnection.jetStream();
                        LOG.info("Connected to JetStream server at: {}", natsServerUrl);
                        // Subscribe to the subject

                        Subscription subscription = tryToSubscribe(jetStream);
                        LOG.info("Subscribed to subject: {}", natsServerUrl);

                        continuouslyReadMessages(subscription, messageDeserializer);
                    } catch (Exception e) {
                        LOG.error("Error during message reading: ", e);
                    }
                });
    }

    private Subscription tryToSubscribe(JetStream jetStream)
            throws IOException, JetStreamApiException, InterruptedException {

        try {
            return jetStream.subscribe(subject);
        } catch (IllegalStateException e) {
            if (e.getMessage().contains(NO_MATCHING_STREAM_CODE)) { // No matching streams for subject
                // try again after 5 seconds
                LOG.warn(
                        "Unable to subscribe to subject: "
                                + subject
                                + " . No matching streams. Trying again in 5sec...");
                Thread.sleep(5000);
                return tryToSubscribe(jetStream);
            }
            throw new RuntimeException(e);
        }
    }

    private void continuouslyReadMessages(
            Subscription subscription, MessageDeserializer messageDeserializer) throws InterruptedException {
        while (true) {
            // Wait for a message
            Message message = subscription.nextMessage(100);
            // Print the message
            if (message != null) {
                try {
                    DeserializedMessage deserializedMessage =
                            messageDeserializer.deserializeMessage(ByteBuffer.wrap(message.getData()));
                    ReadMessage msg =
                            new ReadMessage(
                                    message.getSubject(),
                                    deserializedMessage.name(),
                                    deserializedMessage.content(),
                                    message.metaData().timestamp().toLocalDateTime());
                    messageStore.add(msg);
                    message.ack();
                } catch (Exception e) {
                    LOG.warn("Unable to deserialize message", e);
                }
            }
        }
    }
}
