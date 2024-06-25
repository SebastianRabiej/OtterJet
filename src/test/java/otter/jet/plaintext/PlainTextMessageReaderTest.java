package otter.jet.plaintext;

import com.github.javafaker.Faker;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import otter.jet.AbstractIntegrationTest;
import otter.jet.JetStreamContainerInitializer;
import otter.jet.JetStreamUtils;
import otter.jet.reader.ReadMessage;
import otter.jet.reader.ReaderConfigurationProperties;
import otter.jet.assertions.ComparisonConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import otter.jet.store.Filters;
import otter.jet.store.MessageStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestPropertySource(properties = {"read.mode=plaintext", "read.subject=plaintext"})
class PlainTextMessageReaderTest extends AbstractIntegrationTest {

  private static final LocalDateTime ignoredMessageTimestamp = LocalDateTime.ofInstant(
      Instant.EPOCH, ZoneOffset.UTC);
  @Autowired
  private MessageStore messageStore;
  @Autowired
  private ReaderConfigurationProperties readerConfigurationProperties;

  @Test
  public void shouldReadMessagesSentInPlaintext() {
    // given
    JetStreamUtils.createSubjectStream(readerConfigurationProperties.getSubject(),
        JetStreamContainerInitializer.getNatsServerUrl());
    String randomName = new Faker().name().fullName();
    byte[] data = randomName.getBytes(StandardCharsets.UTF_8);

    // when
    JetStreamUtils.tryToSendMessage(data, readerConfigurationProperties.getSubject(),
        JetStreamContainerInitializer.getNatsServerUrl());

    // then
    await().untilAsserted(() -> assertThat(
        messageStore.filter(Filters.empty(), 0, 10)).usingRecursiveFieldByFieldElementComparator(
        ComparisonConfiguration.configureReadMessageComparison()).contains(
        new ReadMessage(readerConfigurationProperties.getSubject(), "", randomName,
            ignoredMessageTimestamp)));
  }
}
