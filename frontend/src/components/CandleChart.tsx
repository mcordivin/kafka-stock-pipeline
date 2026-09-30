import { useEffect, useRef } from 'react';
import type { Candle } from '../types';
import type { Theme } from '../lib/theme';
import {
    CandlestickSeries,
    ColorType,
    HistogramSeries,
    createChart,
    type IChartApi,
    type ISeriesApi,
    type Time,
    type UTCTimestamp,
  } from 'lightweight-charts';

interface Props {
    history: Candle[];
    live: Candle | null;
    theme: Theme;
}

const cssVar = (name: string) => 
    getComputedStyle(document.documentElement).getPropertyValue(name).trim();

const toTime = (ms: number) => Math.floor(ms/1000) as UTCTimestamp;
const localTime = (t: Time) => new Date((t as number) * 1000).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit'});

const toBar = (c: Candle) => ({
    time: toTime(c.windowStartMillis),
    open: c.open,
    close: c.close,
    high: c.high,
    low: c.low
});

function readPalette() {
    const up = cssVar('--up');
    const down = cssVar('--down');

    return {
        surface: cssVar('--surface'),
        text: cssVar('--text-2'),
        grid: cssVar('--border'),
        up,
        upEdge: cssVar('--up-edge'),
        down,
        downEdge: cssVar('--down-edge'),
        volUp: `${up}99`,
        volDown: `${down}66`,
        font: cssVar('--font-mono'),
    };
}

export function CandleChart({ history, live, theme }: Props) {
    const containerRef = useRef<HTMLDivElement>(null);
    const chartRef = useRef<IChartApi | null>(null);
    const candlesRef = useRef<ISeriesApi<'Candlestick'> | null>(null);
    const volumeRef = useRef<ISeriesApi<'Histogram'> | null>(null);
    const paletteRef = useRef(readPalette());

    const toVolume = (c: Candle) => ({
        time: toTime(c.windowStartMillis),
        value: c.volume,
        color: c.close >= c.open ? paletteRef.current.volUp : paletteRef.current.volDown,
    });

    useEffect(() => {
        const chart = createChart(containerRef.current!, {
            autoSize: true,
            localization: { timeFormatter: localTime },
            timeScale: { timeVisible: true, secondsVisible: false, tickMarkFormatter: localTime },
            rightPriceScale: { scaleMargins: { top: 0.1, bottom: 0.25 } },
        });
        const candles = chart.addSeries(CandlestickSeries);
        const volume = chart.addSeries(HistogramSeries, {
            priceFormat: { type: 'volume'},
            priceScaleId: '',
            lastValueVisible: false,
            priceLineVisible: false,
        });
        volume.priceScale().applyOptions({ scaleMargins: { top: 0.8, bottom: 0 }});

        chartRef.current = chart;
        candlesRef.current = candles;
        volumeRef.current = volume;
        return () => chart.remove();
    }, []);

    useEffect(() => {
        const p = (paletteRef.current = readPalette());
        chartRef.current?.applyOptions({
            layout: {
                background: { type: ColorType.Solid, color: p.surface },
                textColor: p.text,
                fontFamily: p.font,
                attributionLogo: false,
            },
            grid: { vertLines: { color: p.grid }, horzLines: { color: p.grid }},
            rightPriceScale: {borderColor: p.grid},
            timeScale: { borderColor: p.grid},
        });
        candlesRef.current?.applyOptions({
            upColor: p.up,
            borderUpColor: p.upEdge,
            wickUpColor: p.upEdge,
            downColor: p.down,
            borderDownColor: p.downEdge,
            wickDownColor: p.downEdge,
        });

        volumeRef.current?.setData(history.map(toVolume));
    }, [theme, history]);

    useEffect(() => {
        candlesRef.current?.setData(history.map(toBar));
        volumeRef.current?.setData(history.map(toVolume));
        chartRef.current?.timeScale().fitContent();
    }, [history]);

    useEffect(() => {
        if(!live) return;
        try {
            candlesRef.current?.update(toBar(live));
            volumeRef.current?.update(toVolume(live));
        } catch {
            // late update for a window older than last bar
        }
    }, [live]);

    return <div ref={containerRef} className="h-full w-full" />;

}