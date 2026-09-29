package com.stockpipeline.api_server.config;

import com.stockpipeline.api_server.model.Alert;
import com.stockpipeline.api_server.model.Candle;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.group-prefix}")
    private String groupPrefix;

    private final String instanceId = UUID.randomUUID().toString().substring(0, 8);

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Candle> candleLiveFactory() {
        return factory(Candle.class, "candles");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Alert> alertLiveFactory() {
        return factory(Alert.class, "alerts");
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(Class<T> type, String topicLabel) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupPrefix + "-" + topicLabel + "-" + instanceId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        JacksonJsonDeserializer<T> json = new JacksonJsonDeserializer<>(type,false);

        DefaultKafkaConsumerFactory<String, T> consumerFactory = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new ErrorHandlingDeserializer<>(json));

        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        return factory;

    }
}
