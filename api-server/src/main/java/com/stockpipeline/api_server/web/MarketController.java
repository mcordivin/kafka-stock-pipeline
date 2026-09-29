package com.stockpipeline.api_server.web;

import com.stockpipeline.api_server.cache.RedisReader;
import com.stockpipeline.api_server.config.Watchlist;
import com.stockpipeline.api_server.model.Alert;
import com.stockpipeline.api_server.model.Candle;
import com.stockpipeline.api_server.persistence.CandleQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class MarketController {
    private static final Pattern RANGE = Pattern.compile("^(\\d{1,4})([mhdw])$");
    private static final Duration MAX_RANGE = Duration.ofDays(30);
    private static final int MAX_ALERTS = 50;

    private final Watchlist watchlist;
    private final CandleQueryRepository candleQueryRepository;
    private final RedisReader redis;

    public MarketController(Watchlist watchlist, CandleQueryRepository candleQueryRepository, RedisReader redis) {
        this.watchlist = watchlist;
        this.candleQueryRepository = candleQueryRepository;
        this.redis = redis;
    }

    @GetMapping("/symbols")
    public List<String> getSymbols() {
        return watchlist.symbols();
    }

    @GetMapping("/candles/{symbol}")
    public List<Candle> getCandleHistory(@PathVariable("symbol") String symbol,
                                         @RequestParam(defaultValue = "1d") String range) {
        return candleQueryRepository.findRecent(requireWatched(symbol), parseRange(range));
    }

    @GetMapping("/candles/{symbol}/latest")
    public ResponseEntity<Candle> getLatestCandle(@PathVariable("symbol") String symbol) {
        return ResponseEntity.of(redis.latestCandle(requireWatched(symbol)));
    }

    @GetMapping("/alerts/{symbol}")
    public List<Alert> getAlerts(@PathVariable("symbol") String symbol,
                              @RequestParam(defaultValue="20") int limit) {
        return redis.recentAlerts(requireWatched(symbol), Math.max(1, Math.min(limit, MAX_ALERTS)));
    }

    private String requireWatched(String symbol) {
        return watchlist.resolve(symbol).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Symbol not in watchlist: " + symbol));
    }

    private Duration parseRange(String range) {
        Matcher m = RANGE.matcher(range.trim().toLowerCase(Locale.ROOT));
        if(!m.matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Range must look like 30m, 4h, 1d or 1w");
        }

        long n = Long.parseLong(m.group(1));
        Duration d = switch(m.group(2)) {
            case "m" -> Duration.ofMinutes(n);
            case "h" -> Duration.ofHours(n);
            case "d" -> Duration.ofDays(n);
            default -> Duration.ofDays(n * 7);
        };

        if (d.isZero() || d.compareTo(MAX_RANGE) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "range must be between 1m & 30d. Range: " + range);
        }

        return d;
    }

}
