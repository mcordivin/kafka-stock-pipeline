# Kafka Stock Pipeline

A real-time stock market data pipeline demonstrating event streaming architecture with Apache Kafka, Kafka Streams, Redis, and PostgreSQL.

## Overview

This project ingests live stock tick data, processes it through Kafka Streams to compute rolling aggregates (candles) and detect anomalies, then serves the results to a React frontend via a live-updating chart.

## Architecture

```
Market Data API (WebSocket)
        │
        ▼
   [Producer] ──► Kafka topic: stock-ticks
                        │
                        ▼
              [Kafka Streams App]
              - windowed OHLC candle aggregation
              - anomaly/spike detection
                        │
          ┌─────────────┼──────────────┐
          ▼                             ▼
Kafka topic: candles           Kafka topic: alerts
          │                             │
          ▼                             ▼
   [Sink Consumer]                [Alert Consumer]
   - writes to Postgres           - writes to Redis
   - writes latest to Redis
          │
          ▼
   [API/WebSocket Server] ◄── Redis (latest state) + Postgres (history)
          │
          ▼
   [React Frontend] — live chart + alerts
```

## Tech Stack

- **Apache Kafka** (KRaft mode) — event streaming backbone
- **Kafka Streams** (Java/Spring Boot) — stream processing and windowed aggregation
- **Redis** — low-latency cache for latest price/candle state
- **PostgreSQL** — historical candle storage
- **React + TypeScript** — frontend with live charting
- **Docker Compose** — local orchestration

## Getting Started

1. Clone the repo and copy the environment template:
   ```bash
   cp .env.example .env
   ```
   Fill in your Postgres credentials and market data API key in `.env`.

2. Start the infrastructure:
   ```bash
   docker compose up -d
   docker compose ps
   ```
   All services (`kafka`, `kafka-ui`, `redis`, `postgres`) should show `healthy`.

3. Verify Kafka is reachable via the UI at [http://localhost:8080](http://localhost:8080).

4. *(Producer, Streams app, and frontend setup instructions — to be added as those pieces are built.)*

## Project Structure

```
├── docker-compose.yml   # Kafka, Redis, Postgres infrastructure
├── producer/            # Ingests market data, publishes to Kafka
├── streams-app/         # Kafka Streams aggregation & anomaly detection
├── sink-consumer/       # Persists processed data to Postgres/Redis
├── api-server/          # Serves data to frontend via REST/WebSocket
└── frontend/            # React live chart UI
```

## Status

🚧 Work in progress - infrastructure is up and running; producer and streaming logic in development.

## Why This Project

Built to demonstrate practical experience with Kafka's core concepts: partitioning strategy, consumer groups, stream processing (Kafka Streams), and integrating a streaming pipeline with caching (Redis) and persistent storage (Postgres) layers.