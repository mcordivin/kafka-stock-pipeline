package com.stockpipeline.producer.finnhub;

import com.stockpipeline.producer.model.StockTick;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FinnhubStreamManager {

    private static final Logger log = LoggerFactory.getLogger(FinnhubStreamManager.class);

    private final KafkaTemplate<String, StockTick> kafkaTemplate;
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    @Value("${finnhub.api-key}")
    private String finnhubApiKey;

    @Value("${finnhub.ws-url}")
    private String wsBaseUrl;

    @Value("#{'${finnhub.watched-symbols}'.split(',')}")
    private List<String> watchedSymbols;

    @Value("${kafka.topic.stock-ticks}")
    private String stockTicksTopic;

    @Value("${finnhub.reconnect-initial-delay-seconds}")
    private long reconnectInitialDelaySeconds;

    @Value("${finnhub.reconnect-max-delay-seconds}")
    private long reconnectMaxDelaySeconds;

    private FinnhubWebSocketClient client;

    public FinnhubStreamManager(KafkaTemplate<String, StockTick> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void start() {
        connect();
    }

    private void connect() {
        URI uri = URI.create(wsBaseUrl + "?token=" + finnhubApiKey);
        client = new FinnhubWebSocketClient(
                uri,
                watchedSymbols,
                this::publishTrade,
                this::scheduleReconnect
        );
        client.connect();
    }

    private void publishTrade(FinnhubMessageParser.FinnhubTradeMessage.Trade trade) {
        StockTick tick = new StockTick(
                trade.symbol(),
                trade.price(),
                trade.volume(),
                trade.tradeTimestampMillis()
        );

        // TODO: revisit what it's keyed by - per symbol ordering
        kafkaTemplate.send(stockTicksTopic, tick.symbol(), tick);
        consecutiveFailures.set(0);
    }

    private void scheduleReconnect() {
        int attempt = consecutiveFailures.incrementAndGet();
//        This was from claude - what mean?
        long delay = Math.min(
                reconnectInitialDelaySeconds * (1L << Math.min(attempt - 1, 10)),
                reconnectMaxDelaySeconds
        );
        log.info("Reconnecting to Finnhub in {}s (attempt {})", delay, attempt);
        reconnectExecutor.schedule(this::connect, delay, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        reconnectExecutor.shutdownNow();
        if(client != null) {
            client.close();
        }
    }

}
