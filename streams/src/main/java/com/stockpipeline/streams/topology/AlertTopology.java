package com.stockpipeline.streams.topology;

import com.stockpipeline.streams.model.Alert;
import com.stockpipeline.streams.model.Candle;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;
import org.springframework.stereotype.Component;

@Component
public class AlertTopology {
    private final JacksonJsonSerde<Candle> candleSerde;
    private final JacksonJsonSerde<Alert> alertSerde;
    private final String candlesTopic;
    private final String alertTopic;
    private final double thresholdPercent;

    public AlertTopology(JacksonJsonSerde<Candle> candleSerde,
                         JacksonJsonSerde<Alert> alertSerde,
                         @Value("${kafka.topic.candles}") String candlesTopic,
                         @Value("${kafka.topic.alerts}") String alertTopic,
                         @Value("${alerts.threshold-percent}") double thresholdPercent) {
        this.candleSerde = candleSerde;
        this.alertSerde = alertSerde;
        this.candlesTopic = candlesTopic;
        this.alertTopic = alertTopic;
        this.thresholdPercent = thresholdPercent;
    }

    @Autowired
    public void build(StreamsBuilder builder){
        builder.stream(candlesTopic, Consumed.with(Serdes.String(), candleSerde))
                .filter((symbol, c) -> c.open() > 0)
                .mapValues(c -> new Alert(
                        c.symbol(),
                        (c.close() - c.open()) / c.open() * 100,
                        c.open(),
                        c.close(),
                        c.windowStartMillis()))
                .filter((symobl, a) -> Math.abs(a.percentChange()) >= thresholdPercent)
                .to(alertTopic, Produced.with(Serdes.String(), alertSerde));
    }
}
