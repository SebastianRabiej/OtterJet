package otter.jet.store;

import org.junit.jupiter.api.Test;
import otter.jet.reader.ReadMessage;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageStoreTest {

    @Test
    public void limitShouldWork() {
        // Given
        MessageStore store = new DefaultMessageStore(5);

        // When
        store.add(new ReadMessage("test", "test", "1", LocalDateTime.now()));
        store.add(new ReadMessage("test", "test", "2", LocalDateTime.now()));
        store.add(new ReadMessage("test", "test", "3", LocalDateTime.now()));
        store.add(new ReadMessage("test", "test", "4", LocalDateTime.now()));
        store.add(new ReadMessage("test", "test", "5", LocalDateTime.now()));
        store.add(new ReadMessage("test", "test", "6", LocalDateTime.now()));

        List<ReadMessage> filter = store.filter(Filters.empty(), 0, 10);

        // Then
        assertThat(filter.get(0).body()).isEqualTo("6");
        assertThat(filter.get(filter.size() - 1).body()).isEqualTo("2");
        assertThat(filter.stream().anyMatch(e -> e.body().equals("1"))).isFalse();
    }

    @Test
    public void shouldFilterStore() {
        // Given
        MessageStore store = new DefaultMessageStore(100);

        // When
        store.add(new ReadMessage("test", "test", "1", LocalDateTime.now()));
        store.add(new ReadMessage("test", "test", "2", LocalDateTime.now()));
        store.add(new ReadMessage("test-1", "test", "3", LocalDateTime.now()));
        store.add(new ReadMessage("test-1", "test", "4", LocalDateTime.now()));
        store.add(new ReadMessage("test-2", "test", "5", LocalDateTime.now()));
        store.add(new ReadMessage("test-3", "test", "6", LocalDateTime.now()));

        Filters filters = Filters.of("test-1");
        List<ReadMessage> filter = store.filter(filters, 0, 10);

        // Then
        assertThat(filter).hasSize(2);
        assertThat(filter.get(0).body()).isEqualTo("4");
        assertThat(filter.get(1).body()).isEqualTo("3");
    }

    @Test
    public void shouldUseAllFiltersStore() {
        // Given
        MessageStore store = new DefaultMessageStore(100);

        // When
        store.add(new ReadMessage("test", "test", "1", LocalDateTime.now()));
        store.add(new ReadMessage("test", "test", "2", LocalDateTime.now()));
        store.add(new ReadMessage("test-1", "test", "3", LocalDateTime.now()));
        store.add(new ReadMessage("test-1", "test", "4", LocalDateTime.now()));
        store.add(new ReadMessage("test-2", "test", "5", LocalDateTime.now()));
        store.add(new ReadMessage("test-3", "test", "6", LocalDateTime.now()));

        Filters filters = Filters.of("test-1", "test", "3");
        Filters filtersAll = Filters.of("test-1", "test", "5");
        List<ReadMessage> filter = store.filter(filters, 0, 10);
        List<ReadMessage> empty = store.filter(filtersAll, 0, 10);

        // Then
        assertThat(filter).hasSize(1);
        assertThat(filter.get(0).body()).isEqualTo("3");
        assertThat(empty).isEmpty();
    }


    @Test
    public void shouldUsePageAndSize() {
        // Given
        MessageStore store = new DefaultMessageStore(100);

        // When
        store.add(new ReadMessage("test", "test", "1", LocalDateTime.now()));
        store.add(new ReadMessage("test", "test", "2", LocalDateTime.now()));
        store.add(new ReadMessage("test-1", "test", "3", LocalDateTime.now()));
        store.add(new ReadMessage("test-1", "test", "4", LocalDateTime.now()));
        store.add(new ReadMessage("test-2", "test", "5", LocalDateTime.now()));
        store.add(new ReadMessage("test-3", "test", "6", LocalDateTime.now()));

        List<ReadMessage> filter = store.filter(Filters.empty(), 1, 3);

        // Then
        assertThat(filter).hasSize(3);
        assertThat(filter.get(0).body()).isEqualTo("3");
        assertThat(filter.get(1).body()).isEqualTo("2");
        assertThat(filter.get(2).body()).isEqualTo("1");
    }
}
