package com.stockpipeline.streams.model;

public record Alert (
    String symbol,
    double percentChange,
    double open,
    double close,
    long windowStartMillis
) {}
