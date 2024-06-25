package otter.jet.store;

import otter.jet.reader.ReadMessage;

import java.util.List;

public interface MessageStore {

    void add(ReadMessage message);

    List<ReadMessage> filter(Filters filters, int page, int size);
}
