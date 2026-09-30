export interface Candle {
    symbol: string;
    open: number;
    close: number;
    high: number;
    low: number;
    volume: number;
    tickCount: number;
    windowStartMillis: number;
    windowEndMillis: number;
}

// TODO: confirm this matches JSON
export interface PriceAlert {
    symbol: string;
    percentChange: number;
    open: number;
    close: number;
    windowStartMillis: number;
}

export type ServerMessage =
  | { type: 'candle'; data: Candle }
  | { type: 'alert'; data: PriceAlert };