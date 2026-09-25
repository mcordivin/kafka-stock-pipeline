package com.stockpipeline.streams.config;

import com.stockpipeline.streams.model.Alert;
import com.stockpipeline.streams.model.Candle;
import com.stockpipeline.streams.model.StockTick;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;

@Configuration
@EnableKafkaStreams
public class KafkaStreamConfig {

    @SuppressWarnings("resource")
    @Bean(destroyMethod = "close")
    public JacksonJsonSerde<StockTick> tickSerde() {
        return new JacksonJsonSerde<>(StockTick.class).noTypeInfo();
    }

    @SuppressWarnings("resource")
    @Bean(destroyMethod = "close")
    public JacksonJsonSerde<Candle> candleSerde() {
        return new JacksonJsonSerde<>(Candle.class).noTypeInfo();
    }

    @SuppressWarnings("resource")
    @Bean(destroyMethod = "close")
    public JacksonJsonSerde<Alert> AlertSerde() {
        return new JacksonJsonSerde<>(Alert.class).noTypeInfo();
    }

}
