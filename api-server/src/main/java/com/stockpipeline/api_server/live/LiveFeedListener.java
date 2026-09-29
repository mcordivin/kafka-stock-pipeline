package com.stockpipeline.api_server.live;

import com.stockpipeline.api_server.model.Alert;
import com.stockpipeline.api_server.model.Candle;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LiveFeedListener {
    private final LiveWebSocketHandler handler;

    public LiveFeedListener(LiveWebSocketHandler handler) {
        this.handler = handler;
    }

    @KafkaListener(topics = "${kafka.topic.candles}", containerFactory = "candleLiveFactory")
    public void onCandle(Candle candle){
        handler.broadcastCandle(candle);
    }

    @KafkaListener(topics = "${kafka.topic.alerts}", containerFactory = "alertLiveFactory")
    public void onAlert(Alert alert){
        handler.broadcastAlert(alert);
    }
}
