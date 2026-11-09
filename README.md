# Kafka Stock Pipeline

A real-time stock market data pipeline demonstrating event streaming architecture with Apache Kafka, Kafka Streams, Redis, and PostgreSQL.

## Overview

This project ingests live stock tick data from Finnhub, processes it through Kafka Streams to compute 1-minute OHLC candles and detect price anomalies, persists results to PostgreSQL/Redis, and serves a React frontend with live-updating charts.

## Architecture

```
Finnhub WebSocket API
        │
        ▼
   [Producer] ──► Kafka topic: stock-ticks
                        │
                        ▼
              [Kafka Streams App]
              - 1-minute windowed OHLC candle aggregation
              - Price spike/anomaly detection (configurable threshold)
                        │
          ┌─────────────┴──────────────┐
          ▼                            ▼
Kafka topic: candles          Kafka topic: alerts
          │                            │
          └──────────┬─────────────────┘
                     ▼
              [Sink Consumer]
              - Candles → PostgreSQL (history) + Redis (latest)
              - Alerts → Redis (recent alerts list)
                     │
                     ▼
          [API Server] ◄── Redis + PostgreSQL
          - REST: /symbols, /candles/{symbol}, /alerts/{symbol}
          - WebSocket: /ws/live (real-time candle & alert updates)
                     │
                     ▼
          [React Frontend]
          - Live candlestick chart (lightweight-charts)
          - Real-time price alerts
          - Dark/light theme toggle
```

## Tech Stack

| Component | Technology |
|-----------|------------|
| Event Streaming | Apache Kafka (KRaft mode) |
| Stream Processing | Kafka Streams + Spring Boot 4.1 |
| Cache | Redis 7 |
| Database | PostgreSQL 16 |
| Backend | Spring Boot 4.1 (Java 21) |
| Frontend | React 19 + TypeScript + Vite |
| Charts | TradingView lightweight-charts |
| Containerization | Docker Compose |

## Project Structure

```
kafka-stock-pipeline/
├── docker-compose.yml      # Kafka, Redis, PostgreSQL infrastructure
├── producer/               # Finnhub WebSocket → Kafka producer
├── streams/                # Kafka Streams: candle aggregation & alerts
├── sink/                   # Kafka consumer → PostgreSQL + Redis
├── api-server/             # REST API + WebSocket server
├── frontend/               # React live chart UI
└── package.json            # Root dependencies (Tailwind, lightweight-charts)
```

## Getting Started

### Prerequisites

- Java 21
- Node.js 20+
- Docker & Docker Compose
- Finnhub API key (free at [finnhub.io](https://finnhub.io))

### 1. Clone and Configure

```bash
git clone <repo-url>
cd kafka-stock-pipeline
cp .env.example .env
```

Edit `.env` with your credentials:
```env
FINNHUB_API_KEY=your_api_key
POSTGRES_PASSWORD=your_password
```

### 2. Start Infrastructure

```bash
docker compose up -d
docker compose ps   # All services should show "healthy"
```

Services:
- **Kafka**: localhost:9092
- **Kafka UI**: http://localhost:8080
- **Redis**: localhost:6379
- **PostgreSQL**: localhost:5432

### 3. Run the Backend Services

Start each service in a separate terminal:

```bash
# Terminal 1: Producer (Finnhub → Kafka)
cd producer
./mvnw spring-boot:run

# Terminal 2: Streams (Kafka Streams processing)
cd streams
./mvnw spring-boot:run

# Terminal 3: Sink (Kafka → PostgreSQL/Redis)
cd sink
./mvnw spring-boot:run

# Terminal 4: API Server (REST + WebSocket)
cd api-server
./mvnw spring-boot:run
```

### 4. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 to view the live dashboard.

## Configuration

### Watched Symbols

Edit `producer/src/main/resources/application.yml`:
```yaml
finnhub:
  watched-symbols: AAPL,MSFT,GOOGL,TSLA,AMZN
```

### Alert Threshold

Edit `streams/src/main/resources/application.yaml`:
```yaml
alerts:
  threshold-percent: 0.5   # Trigger alert on 0.5%+ price change
```

### Service Ports

| Service | Port |
|---------|------|
| Producer | 8080 |
| Streams | 8081 |
| Sink | 8082 |
| API Server | 8083 |
| Frontend (dev) | 5173 |

## API Endpoints

### REST

| Endpoint | Description |
|----------|-------------|
| `GET /symbols` | List watched symbols |
| `GET /candles/{symbol}?range=1d` | Historical candles (range: 30m, 4h, 1d, 1w) |
| `GET /candles/{symbol}/latest` | Latest candle from Redis |
| `GET /alerts/{symbol}?limit=20` | Recent price alerts |

### WebSocket

Connect to `ws://localhost:8083/ws/live`

```json
// Subscribe
{"action": "subscribe", "symbol": "AAPL"}

// Server sends
{"type": "candle", "symbol": "AAPL", "data": {...}}
{"type": "alert", "symbol": "AAPL", "data": {...}}
```

## Data Models

### StockTick (Kafka: stock-ticks)
```json
{
  "symbol": "AAPL",
  "price": 178.52,
  "volume": 100,
  "tradeTimestampMillis": 1696012800000
}
```

### Candle (Kafka: candles)
```json
{
  "symbol": "AAPL",
  "open": 178.00,
  "high": 179.25,
  "low": 177.80,
  "close": 178.52,
  "volume": 125000,
  "tickCount": 342,
  "windowStartMillis": 1696012800000,
  "windowEndMillis": 1696012860000
}
```

### Alert (Kafka: alerts)
```json
{
  "symbol": "AAPL",
  "percentChange": 1.25,
  "open": 176.50,
  "close": 178.70,
  "windowStartMillis": 1696012800000
}
```

## Development

### Build All Services

```bash
# Backend
for dir in producer streams sink api-server; do
  (cd $dir && ./mvnw clean package -DskipTests)
done

# Frontend
cd frontend && npm run build
```

### Run Tests

```bash
cd <service>
./mvnw test
```

## Troubleshooting

### WebSocket Connection Issues

If the frontend can't connect to WebSocket:
1. Ensure api-server is running on port 8083
2. Check CORS settings in `api-server/src/main/resources/application.yaml`
3. Restart Vite dev server after config changes

### No Data Showing

1. Check Kafka UI (http://localhost:8080) for messages in topics
2. Verify Finnhub API key is valid
3. Ensure market is open (Finnhub only sends data during trading hours)

### Database Migrations

Flyway runs automatically on sink startup. To reset:
```bash
docker compose down -v   # Removes volumes
docker compose up -d
```

## License

MIT
