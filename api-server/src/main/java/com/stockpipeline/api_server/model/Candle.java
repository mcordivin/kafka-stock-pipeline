package com.stockpipeline.api_server.model;

public record Candle (
        String symbol,
        double open,
        double close,
        double high,
        double low,
        long volume,
        long tickCount,
        long windowStartMillis,
        long windowEndMillis
) {}
