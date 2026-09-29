package com.stockpipeline.sink.model;

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
) {}
