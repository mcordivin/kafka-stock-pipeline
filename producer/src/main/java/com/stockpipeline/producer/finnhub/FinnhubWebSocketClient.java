package com.stockpipeline.producer.finnhub;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

public class FinnhubWebSocketClient extends WebSocketClient {
    private static final Logger log = LoggerFactory.getLogger(FinnhubWebSocketClient.class);
    private final List<String> watchedSymbols;
    private final Consumer<FinnhubMessageParser.FinnhubTradeMessage.Trade> onTrade;
    private final Runnable onDisconnect;


    public FinnhubWebSocketClient(
            URI serverUri,
            List<String> watchedSymbols,
            Consumer<FinnhubMessageParser.FinnhubTradeMessage.Trade> onTrade,
            Runnable onDisconnect
    ) {
        super(serverUri);
        this.watchedSymbols = watchedSymbols;
        this.onTrade = onTrade;
        this.onDisconnect = onDisconnect;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("Connected to Finnhub Websocket, subscribing to {} symbols: {}",
                watchedSymbols.size(), watchedSymbols);

        for (String symbol : watchedSymbols) {
            send("{\"type\":\"subscribe\",\"symbol\":\"" + symbol + "\"}");
        }
    }

//    TODO: ponder
    @Override
    public void onMessage(String s) {
        FinnhubMessageParser.FinnhubTradeMessage parsed = FinnhubMessageParser.parse(s);
        if(parsed == null) {
            return;
        }

        if("ping".equals(parsed.type())) {
            return;
        }

        if(!"trade".equals(parsed.type()) || parsed.data() == null) {
            return;
        }

        for(FinnhubMessageParser.FinnhubTradeMessage.Trade trade: parsed.data()) {
            onTrade.accept(trade);
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        log.warn("Finnhub websocket closed (code={}, reason={}, remote={})", i, s, b);
        onDisconnect.run();
    }

    @Override
    public void onError(Exception e) {
        log.error("Finnhub websocket error", e);
    }
}
