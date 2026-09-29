CREATE TABLE IF NOT EXISTS candles (
                                       symbol        VARCHAR(32)      NOT NULL,
    window_start  TIMESTAMPTZ      NOT NULL,
    window_end    TIMESTAMPTZ      NOT NULL,
    open          NUMERIC(19, 6)   NOT NULL,
    close         NUMERIC(19, 6)   NOT NULL,
    high          NUMERIC(19, 6)   NOT NULL,
    low           NUMERIC(19, 6)   NOT NULL,
    volume        BIGINT           NOT NULL,
    tick_count    BIGINT           NOT NULL,
    -- one row per symbol per minute; also makes redelivered candles idempotent
    -- and serves the api-server's "WHERE symbol = ? AND window_start >= ?" query
    PRIMARY KEY (symbol, window_start)
    );