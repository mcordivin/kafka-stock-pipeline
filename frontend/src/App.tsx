// import { useState } from 'react'

import { useState, useEffect, useCallback } from "react";
import { applyTheme, initialTheme, type Theme } from './lib/theme';
import type { Candle, PriceAlert, ServerMessage } from "./types";
import { fetchSymbols, fetchCandles } from "./lib/api";
import { useLiveFeed } from "./hooks/useLiveFeed";
import { ConnectionStatus } from "./components/ConnectionStatus";
import { SymbolPicker } from "./components/SymbolPicker";
import { ThemeToggle } from "./components/ThemeToggle";
import { PriceHeader } from "./components/PriceHeader";
import { CandleChart } from "./components/CandleChart";
import { AlertFeed } from "./components/AlertFeed";
import { AlertToast } from "./components/AlertToast";

const MAX_ALERTS = 50;

function App() {
  const [theme, setTheme] = useState<Theme>(() => {
    const t = initialTheme();
    applyTheme(t);
    return t;
  });

  const [symbols, setSymbols] = useState<string[]>([]);
  const [symbol, setSymbol] = useState<string | null>(null);
  const [history, setHistory] = useState<Candle[]>([]);
  const [live, setLive] = useState<Candle | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [alerts, setAlerts] = useState<PriceAlert[]>([]);
  const [toasts, setToasts] = useState<PriceAlert[]>([]);

  const toggleTheme = () => {
    const next = theme === 'dark' ? 'light' : 'dark';
    applyTheme(next);
    setTheme(next);
  }

  const selectSymbol = (next: string) => {
    setSymbol(next);
    setLive(null);
    setError(null);
  }

useEffect(() => {
    const ctrl = new AbortController();
    fetchSymbols(ctrl.signal)
    .then((list) => {
      setSymbols(list);
      setSymbol((s) => s ?? list[0] ?? null);
    })
    .catch((e) => !ctrl.signal.aborted && setError(`Couldn't load symbols: ${e.message}`));
    return () => ctrl.abort();
  }, []);

useEffect(() => {
    if(!symbol) return;
    const ctrl = new AbortController();
    fetchCandles(symbol, '1d', ctrl.signal)
    .then(setHistory)
    .catch((e) => {
      if(ctrl.signal.aborted) return;
      setHistory([]);
      setError(`Coudln't load history for ${symbol} : ${e.message}`);
    });
    return () => ctrl.abort();
  }, [symbol]);

const onMessage = useCallback(
  (msg: ServerMessage) => {
    if(msg.data.symbol !== symbol) return;
    if(msg.type === 'candle') {
      setLive(msg.data);
    } else {
      setAlerts((prev) => [msg.data, ...prev].slice(0, MAX_ALERTS));
      setToasts((prev) => [...prev, msg.data].slice(-3));
    }
  },
  [symbol],
)

const connection = useLiveFeed(symbol, onMessage);

const dismissToast = useCallback(
  (a: PriceAlert) => setToasts((prev) => prev.filter((t) => t !== a)),
  [],
);

const last = live ?? history.at(-1);
const isEmpty = history.length === 0 && !live;

  return (
    <div className="flex min-h-dvh flex-col">
      <header className="flex flex-wrap items-center gap-4 border-b border-line px-5 py-3 sm:px-8">
        <span className="font-display text-lg font-semibold">Stock pipeline</span>
        <div className="ml-auto flex flex-wrap items-center gap-4">
          <ConnectionStatus connection={connection} />
          <SymbolPicker symbols={symbols} value={symbol} onChange={selectSymbol} />
          <ThemeToggle theme={theme} onToggle={toggleTheme} />
        </div>
      </header>

      <main className="grid flex-1 gap-8 px-5 py-8 sm:px-8 lg:grid-cols-[minmax(0,1fr)_20rem]">
        <div className="flex min-w-0 flex-col gap-6">
          <PriceHeader symbol={symbol} first={history[0]} last={last} />

          {error && (
            <p role="alert" className="rounded-card border border-line bg-surface px-4 py-3 text-sm">
              {error}. Make sure everyone is up & running.
            </p>
          )}

          <div className="relative h-112 overflow-hidden rounded-card border border-line bg-surface">
            <CandleChart history={history} live={live} theme={theme} />
            {isEmpty && !error && (
              <div className="absolute inset-0 grid place-items-center p-6 text-center">
                <p className="max-w-sm text-sm text-fg-2">
                  No candles for {symbol ?? 'this symbol'}. Trades are made during market hours - new candles forms every minute.
                </p>
              </div>
            )}
          </div>
        </div>

        <aside className="lg:border lg:border-line lg:pl-8">
          <AlertFeed alerts={alerts}/>
        </aside>
      </main>

      <AlertToast toasts={toasts} onDismiss={dismissToast}/>

    </div>
  )
}

export default App
