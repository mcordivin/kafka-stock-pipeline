package com.stockpipeline.api_server.model;

public record Alert (
        String symbol,
        double percentChange,
        double open,
        double close,
        long windowStartMillis
) {}
