package com.stockpipeline.api_server.cache;

import com.stockpipeline.api_server.model.Alert;
import com.stockpipeline.api_server.model.Candle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class RedisReader {
    private static final Logger log = LoggerFactory.getLogger(RedisReader.class);
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisReader(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    public Optional<Candle> latestCandle(String symbol) {
        return Optional.ofNullable(redis.opsForValue().get("latest:" + symbol))
                .map(json -> read(json, Candle.class));
    }

    public List<Alert> recentAlerts(String symbol, int limit) {
        List<String> raw = redis.opsForList().range("alerts:" + symbol, 0, limit - 1);
        if (raw == null) {
            return List.of();
        }
        return raw.stream().map(json -> read(json, Alert.class)).filter(Objects::nonNull).toList();
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("Unreadable {} in Redis, skipping: {}", type.getSimpleName(), json, e);
            return null;
        }
    }
}
