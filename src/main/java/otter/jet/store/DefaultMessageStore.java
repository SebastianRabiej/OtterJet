package otter.jet.store;

import org.springframework.beans.factory.annotation.Value;
import otter.jet.reader.ReadMessage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

public class DefaultMessageStore implements MessageStore {

    private final Deque<ReadMessage> messages = new ArrayDeque<>();
    private final int limit;

    public DefaultMessageStore(@Value("${read.store.limit:1000}") int limit) {
        this.limit = limit;
    }

    public void add(ReadMessage message) {
        if (messages.size() >= limit) {
            messages.removeLast();
        }
        messages.addFirst(message);
    }

    public List<ReadMessage> filter(Filters filters, int page, int size) {
        return filter(filters.toPredicate(), page, size);
    }

    private List<ReadMessage> filter(Predicate<ReadMessage> predicate, int page, int size) {
        return messages.stream()
                .filter(predicate)
                .skip((long) page * size)
                .limit(size)
                .toList();
    }



}
