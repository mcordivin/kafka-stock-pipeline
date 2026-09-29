package com.stockpipeline.sink.persistence;

import com.stockpipeline.sink.model.Candle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Repository
public class CandleRepository {

    private static final String UPSERT = """
            INSERT INTO candles
                (symbol, window_start, window_end, open, close, high, low, volume, tick_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (symbol, window_start) DO UPDATE SET
                window_end = EXCLUDED.window_end,
                open       = EXCLUDED.open,
                close      = EXCLUDED.close,
                high       = EXCLUDED.high,
                low        = EXCLUDED.low,
                volume     = EXCLUDED.volume,
                tick_count = EXCLUDED.tick_count
            """;

    private final JdbcTemplate jdbc;

    public CandleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public void upsert(Candle candle) {
        jdbc.update(UPSERT,
                candle.symbol(),
                toUtc(candle.windowStartMillis()),
                toUtc(candle.windowEndMillis()),
                candle.open(),
                candle.close(),
                candle.high(),
                candle.low(),
                candle.volume(),
                candle.tickCount());
    }

    private static OffsetDateTime toUtc(Long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atOffset(ZoneOffset.UTC);
    }
}
