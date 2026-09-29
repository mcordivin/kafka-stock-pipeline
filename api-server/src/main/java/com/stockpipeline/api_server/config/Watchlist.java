package com.stockpipeline.api_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class Watchlist {
    private final List<String> symbols;

    public Watchlist(@Value("${watchlist.symbols}") String raw) {
        this.symbols = Arrays.stream(raw.split(", "))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    public List<String> symbols() {
        return symbols;
    }

    public Optional<String> resolve(String symbol) {
        if(symbol == null) {
            return Optional.empty();
        }

        String upper = symbol.trim().toUpperCase(Locale.ROOT);
        return symbols.contains(upper) ? Optional.of(upper) : Optional.empty();
    }
}
