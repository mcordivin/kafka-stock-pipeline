package com.stockpipeline.sink.config;

import com.stockpipeline.sink.model.Alert;
import com.stockpipeline.sink.model.Candle;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    private final String bootstrapServers;

    public KafkaConsumerConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Candle> candleListenerFactory() {
        return factoryFor(Candle.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Alert> alertListenerFactory() {
        return factoryFor(Alert.class);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factoryFor(Class<T> type) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        JacksonJsonDeserializer<T> json = new JacksonJsonDeserializer<>(type, false);
        ErrorHandlingDeserializer<T> value = new ErrorHandlingDeserializer<>(json);

        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), value));
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
    }
}
