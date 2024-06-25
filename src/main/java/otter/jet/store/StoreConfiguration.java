package otter.jet.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class StoreConfiguration {

    @Bean
    public MessageStore messageStore(
            @Value("${read.store.limit:1000}") Integer limit) {
        return new DefaultMessageStore(limit);
    }
}
