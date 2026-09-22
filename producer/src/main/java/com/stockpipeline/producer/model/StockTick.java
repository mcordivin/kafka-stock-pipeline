package com.stockpipeline.producer.model;

public record StockTick(
        String symbol,
        double price,
        long volume,
        // UNIX
        long tradeTimestampMillis
) {
}
