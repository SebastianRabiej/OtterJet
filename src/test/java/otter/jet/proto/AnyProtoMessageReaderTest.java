package otter.jet.proto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.protobuf.Any;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import otter.jet.AbstractIntegrationTest;
import otter.jet.JetStreamContainerInitializer;
import otter.jet.JetStreamUtils;
import otter.jet.reader.ReadMessage;
import otter.jet.reader.ReaderConfigurationProperties;
import otter.jet.assertions.ComparisonConfiguration;
import otter.jet.examples.RandomProtoPersonGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import otter.jet.examples.protobuf.PersonProtos.Person;
import otter.jet.store.Filters;
import otter.jet.store.MessageStore;

@TestPropertySource(
    properties = {
      "read.mode=proto",
      "read.subject=any_person",
      "read.proto.pathToDescriptor=src/test/resources/person.desc",
      "read.beginTimestamp="
    })
class AnyProtoMessageReaderTest extends AbstractIntegrationTest {

  private static final LocalDateTime ignoredMessageTimestamp =
      LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
  @Autowired private MessageStore messageStore;
  @Autowired private ReaderConfigurationProperties readerConfigurationProperties;

  @Test
  public void shouldReadProtoMessageSentAsAny() {
    // given
    JetStreamUtils.createSubjectStream(
        readerConfigurationProperties.getSubject(),
        JetStreamContainerInitializer.getNatsServerUrl());
    Person person = RandomProtoPersonGenerator.randomPerson();
    byte[] data = Any.pack(person).toByteArray();

    // when
    JetStreamUtils.tryToSendMessage(
        data,
        readerConfigurationProperties.getSubject(),
        JetStreamContainerInitializer.getNatsServerUrl());

    // then
    await()
        .untilAsserted(
            () ->
                assertThat(messageStore.filter(Filters.empty(), 0, 10))
                    .usingRecursiveFieldByFieldElementComparator(
                        ComparisonConfiguration.configureReadMessageComparisonWithJSONBody())
                    .contains(
                        new ReadMessage(
                            readerConfigurationProperties.getSubject(),
                            "Person",
                            new JSONObject()
                                .put("id", person.getId())
                                .put("name", person.getName())
                                .put("email", person.getEmail())
                                .put("numbers", new JSONArray().put(person.getNumbers(0)))
                                .toString(),
                            ignoredMessageTimestamp)));
  }
}
