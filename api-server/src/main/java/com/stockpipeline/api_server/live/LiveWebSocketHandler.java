package com.stockpipeline.api_server.live;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.stockpipeline.api_server.cache.RedisReader;
import com.stockpipeline.api_server.config.Watchlist;
import com.stockpipeline.api_server.model.Alert;
import com.stockpipeline.api_server.model.Candle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * /ws/live protocol
 *   client -> {"action":"subscribe","symbol":"AAPL"}   (replaces any previous symbol)
 *   client -> {"action":"unsubscribe"}
 *   server -> {"type":"subscribed","symbol":"AAPL","data":null}
 *   server -> {"type":"candle","symbol":"AAPL","data":{...}}   (snapshot from Redis, then live)
 *   server -> {"type":"alert","symbol":"AAPL","data":{...}}
 *   server -> {"type":"error","symbol":null,"data":"..."}
 */
@Component
public class LiveWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(LiveWebSocketHandler.class);

    // WebSocketSession.sendMessage isn't thread-safe; the decorator serializes
    // sends and drops clients that fall too far behind (slow-consumer protection)
    private static final int SEND_TIME_LIMIT_MS = 5_000;
    private static final int SEND_BUFFER_LIMIT_BYTES = 512 * 1024;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> subscriptions = new ConcurrentHashMap<>();

    private final ObjectMapper mapper;
    private final Watchlist watchlist;
    private final RedisReader redis;

    public LiveWebSocketHandler(ObjectMapper mapper, Watchlist watchlist, RedisReader redis) {
        this.mapper = mapper;
        this.watchlist = watchlist;
        this.redis = redis;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ClientMessage(String action, String symbol){}

    record ServerMessage(String type, String symbol, Object data){}

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(),
                new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT_MS, SEND_BUFFER_LIMIT_BYTES));
        log.info("WS connected {} ({} open)", session.getId(), sessions.size());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String sessionId = session.getId();
        ClientMessage msg;

        try {
            msg = mapper.readValue(message.getPayload(), ClientMessage.class);
        } catch (Exception e) {
            send(sessionId, new ServerMessage("error", null, "invalid JSON"));
            return;
        }

        if ("subscribe".equals(msg.action())) {
            watchlist.resolve(msg.symbol()).ifPresentOrElse(symbol -> {
                subscriptions.put(sessionId, symbol);
                send(sessionId, new ServerMessage("subscribed", symbol, null));
                // snapshot so the client doesn't wait up to a minute for the first bar
                redis.latestCandle(symbol).ifPresent(c -> send(sessionId, new ServerMessage("candle", symbol, c)));
            }, () -> send(sessionId, new ServerMessage("error", null, "symbol not in watchlist: " + msg.symbol())));
        } else if ("unsubscribe".equals(msg.action())) {
            subscriptions.remove(sessionId);
        } else {
            send(sessionId, new ServerMessage("error", null, "unknown action: " + msg.action()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        remove(session.getId());
        log.info("WS disconnected {} {} ({} closed)", session.getId(), status, sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.debug("WS transport error {}", session.getId(), exception);
        remove(session.getId());
    }

    public void broadcastCandle(Candle candle) {
        broadcast(candle.symbol(), new ServerMessage("candle", candle.symbol(), candle));
    }

    public void broadcastAlert(Alert alert) {
        broadcast(alert.symbol(), new ServerMessage("alert", alert.symbol(), alert));
    }

    public void broadcast(String symbol, ServerMessage message) {
        String json = toJson(message);
        if(json == null) return;

        subscriptions.forEach((sessionId, subscribed) -> {
            if(subscribed.equals(symbol)) {
                sendRaw(sessionId, json);
            }
        });
    }

    private void send(String sessionId, ServerMessage message) {
        String json = toJson(message);
        if(json!=null) {
            sendRaw(sessionId, json);
        }
    }

    private void sendRaw(String sessionId, String json) {
        WebSocketSession session = sessions.get(sessionId);
        if(session == null || !session.isOpen()) {
            remove(sessionId);
            return;
        }

        try {
            session.sendMessage(new TextMessage(json));
        } catch(Exception e) {
            log.info("Dropping WS client {}: {}", sessionId, e.getMessage());
            remove(sessionId);
        }
    }

    private void remove(String sessionId) {
        subscriptions.remove(sessionId);
        sessions.remove(sessionId);
    }

    private String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Failed to serialize {}", object ,e);
            return null;
        }
    }
}
