package com.stockpipeline.sink.cache;

import com.stockpipeline.sink.model.Alert;
import com.stockpipeline.sink.model.Candle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Component
public class RedisStore {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final int alertsMaxPerSymbol;
    private final Duration alertsTtl;

    public RedisStore(StringRedisTemplate redis,
                      ObjectMapper mapper,
                      @Value("${redis.alerts.max-per-symbol}") int alertsMaxPerSymbol,
                      @Value("${redis.alerts.ttl-hours}") long alertsTtlHours) {
        this.redis = redis;
        this.mapper = mapper;
        this.alertsMaxPerSymbol = alertsMaxPerSymbol;
        this.alertsTtl = Duration.ofHours(alertsTtlHours);
    }

    public void setLatestCandle(Candle candle) {
        redis.opsForValue().set("latest:" + candle.symbol(), toJson(candle));
    }

    public void pushAlert(Alert alert) {
        String key = "alerts:" + alert.symbol();
        redis.opsForList().leftPush(key, toJson(alert));
        redis.opsForList().trim(key, 0, alertsMaxPerSymbol - 1);
        redis.expire(key, alertsTtl);
    }

    private String toJson(Object value) {
        return mapper.writeValueAsString(value);
    }

}
