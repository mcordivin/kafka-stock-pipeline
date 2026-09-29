package com.stockpipeline.api_server.persistence;

import com.stockpipeline.api_server.model.Candle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class CandleQueryRepository {

    private static final String HISTORY = """
            
            SELECT symbol, window_start, window_end, open, close, high, low, volume, tick_count
            FROM candles
            WHERE symbol = ?
                AND window_start >= (SELECT MAX(window_start) FROM candles WHERE symbol = ?)
                    - (? * INTERVAL '1 second')
            ORDER BY window_start
            """;

    private static final RowMapper<Candle> ROW_MAPPER = (rs, rowNumber) -> new Candle(
            rs.getString("symbol"),
            rs.getDouble("open"),
            rs.getDouble("close"),
            rs.getDouble("high"),
            rs.getDouble("low"),
            rs.getLong("volume"),
            rs.getLong("tick_count"),
            rs.getObject("window_start", OffsetDateTime.class).toInstant().toEpochMilli(),
            rs.getObject("window_end", OffsetDateTime.class).toInstant().toEpochMilli()
    );

    private final JdbcTemplate jdbc;

    public CandleQueryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Candle> findRecent(String symbol, Duration range) {
        return jdbc.query(HISTORY, ROW_MAPPER, symbol, symbol, (double) range.toSeconds());
    }
}
