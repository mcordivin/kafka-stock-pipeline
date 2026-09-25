package com.stockpipeline.streams.topology;

import com.stockpipeline.streams.config.TradeTimestampExtractor;
import com.stockpipeline.streams.model.Candle;
import com.stockpipeline.streams.model.StockTick;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;
import org.springframework.stereotype.Component;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.WindowStore;

import java.time.Duration;

@Component
public class CandleTopology {
    private final JacksonJsonSerde<StockTick> tickSerde;
    private final JacksonJsonSerde<Candle> candleSerde;
    private final String ticksTopic;
    private final String candlesTopic;

    public CandleTopology(JacksonJsonSerde<StockTick> tickSerde,
                          JacksonJsonSerde<Candle> candleSerde,
                          @Value("${kafka.topic.stock-ticks}") String ticksTopic,
                          @Value("${kafka.topic.candles}") String candlesTopic) {
        this.tickSerde = tickSerde;
        this.candleSerde = candleSerde;
        this.ticksTopic = ticksTopic;
        this.candlesTopic = candlesTopic;
    }

    @Autowired
    public void build(StreamsBuilder builder) {
        builder.stream(ticksTopic, Consumed.with(Serdes.String(), tickSerde)
                        .withTimestampExtractor(new TradeTimestampExtractor()))
                .groupByKey(Grouped.with(Serdes.String(), tickSerde))
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)))
                .aggregate(
                        Candle::empty,
                        (symbol, tick, candle) -> candle.update(tick),
                        Materialized.<String, Candle, WindowStore<Bytes, byte[]>>as("candle-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(candleSerde))
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded())
                        .withName("candle-suppress"))
                .toStream()
                .map((windowedKey, candle) -> KeyValue.pair(
                        windowedKey.key(),
                        candle.withWindow(windowedKey.window().start(), windowedKey.window().end())))
                .to(candlesTopic, Produced.with(Serdes.String(), candleSerde));
    }
}
