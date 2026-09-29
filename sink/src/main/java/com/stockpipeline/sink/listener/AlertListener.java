package com.stockpipeline.sink.listener;

import com.stockpipeline.sink.cache.RedisStore;
import com.stockpipeline.sink.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertListener {
    private static final Logger log = LoggerFactory.getLogger(AlertListener.class);
    private final RedisStore redisStore;

    public AlertListener(RedisStore redisStore) {
        this.redisStore = redisStore;
    }

    @KafkaListener(
            topics = "${kafka.topic.alerts}",
            groupId = "${kafka.group.alerts",
            containerFactory = "alertListenerFactory"
    )
    public void onAlert(Alert alert){
        redisStore.pushAlert(alert);
        log.info("Stored alert {} {}% @ {}",
                alert.symbol(), String.format("%.3f", alert.percentChange()), alert.windowStartMillis());
    }
}
