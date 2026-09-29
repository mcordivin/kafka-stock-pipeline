package com.stockpipeline.sink.listener;

import com.stockpipeline.sink.cache.RedisStore;
import com.stockpipeline.sink.model.Candle;
import com.stockpipeline.sink.persistence.CandleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CandleListener {
    private static final Logger log = LoggerFactory.getLogger(CandleListener.class);
    private final CandleRepository candleRepository;
    private final RedisStore redisStore;

    public CandleListener(CandleRepository candleRepository, RedisStore redisStore) {
        this.candleRepository = candleRepository;
        this.redisStore = redisStore;
    }

    @KafkaListener(
            topics = "${kafka.topic.candles}",
            groupId = "${kafka.group.candles}",
            containerFactory = "candleListenerFactory"
    )
    public void onCandle(Candle candle) {
        candleRepository.upsert(candle);
        redisStore.setLatestCandle(candle);
        log.info("Stored candle {} @ {} O={} C={} H={} L={} V={}",
                candle.symbol(), candle.windowStartMillis(), candle.open(), candle.close(),
                candle.high(), candle.low(), candle.volume());
    }
}
