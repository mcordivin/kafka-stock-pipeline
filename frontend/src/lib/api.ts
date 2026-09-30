import type { Candle, ServerMessage } from '../types';

const API_BASE = import.meta.env.VITE_API_BASE ?? '';

export const WS_URL = import.meta.env.VITE_WS_URL ?? 
    `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws/live`;

async function getJson<T>(path: string, signal?: AbortSignal): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, {signal});
    if(!res.ok) throw new Error(`${path} returned ${res.status}`);
    return res.json() as Promise<T>;
}

export const fetchSymbols = (signal?: AbortSignal) => getJson<string[]>('/symbols', signal);

export async function fetchCandles(symbol: string, range = '1d', signal?: AbortSignal) {
    const candles = await getJson<Candle[]> (
        `/candles/${encodeURIComponent(symbol)}?range=${range}`,
        signal,
    );

    const byStart = new Map(candles.map((c) => [c.windowStartMillis, c]));
    return Array.from(byStart.values()).sort((a, b) => a.windowStartMillis - b.windowStartMillis);
}

export function parseServerMessage(raw: unknown): ServerMessage | null {
    if (typeof raw !== 'string') return null;

    try {
        const msg = JSON.parse(raw);
        return msg?.type === 'candle' || msg?.type === 'alert' ? (msg as ServerMessage) : null;
    } catch {
        return null;
    }
}