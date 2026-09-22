package com.stockpipeline.producer.finnhub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class FinnhubMessageParser {
    private static final Logger log = LoggerFactory.getLogger(FinnhubMessageParser.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private FinnhubMessageParser() {}

    public static FinnhubTradeMessage parse(String raw){
        try {
            return objectMapper.readValue(raw, FinnhubTradeMessage.class);
        } catch (Exception e) {
            log.warn("Failed to parse Finnhub message, skipping: {}", raw, e);
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FinnhubTradeMessage(String type, List<Trade> data) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Trade(
                @JsonProperty("s") String symbol,
                @JsonProperty("p") double price,
                @JsonProperty("v") long volume,
                @JsonProperty("t") long tradeTimestampMillis
        ) {}

    }
}

