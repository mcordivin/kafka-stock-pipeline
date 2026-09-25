package com.stockpipeline.streams.model;

public record Candle (
        String symbol,
        double open,
        double close,
        double high,
        double low,
        long volume,
        long tickCount,
        long firstTradeMillis,
        long lastTradeMillis,
        long windowStartMillis,
        long windowEndMillis
) {

    public static Candle empty() {
        return new Candle(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public Candle update(StockTick tick) {
        long timestamp = tick.tradeTimestampMillis();
        double price = tick.price();

        if(tickCount == 0) {
            return new Candle(tick.symbol(), price, price, price, price, tick.volume(), 1, timestamp, timestamp, 0, 0);
        }

        boolean earliest = timestamp < firstTradeMillis;
        boolean latest = timestamp > lastTradeMillis;

        return new Candle (
                symbol,
                earliest ? price : open,
                Math.max(high, price),
                Math.min(low, price),
                latest ? price : close,
                volume + tick.volume(),
                tickCount + 1,
                earliest ? timestamp : firstTradeMillis,
                latest ? timestamp : lastTradeMillis,
                windowStartMillis,
                windowEndMillis
        );
    }

    public Candle withWindow(long startMillis, long endMillis) {
        return new Candle(symbol, open, high, low, close, volume, tickCount, firstTradeMillis, lastTradeMillis, startMillis, endMillis);
    }

}
