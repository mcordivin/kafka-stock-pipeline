import { formatPercent, formatPrice, formatTime } from "../lib/format";
import type { Candle } from "../types";

interface Props {
    symbol: string | null;
    first: Candle | undefined;
    last: Candle | undefined;
}

export function PriceHeader({ symbol, first, last}: Props){
    const change = first && last ? ((last.close - first.open) / first.open) * 100 : null;

    return (
        <div className="flex flex-wrap items-end gap-x-6 gap-y-2">
            <h1 className="font-display text-6xl leading-none font-semibold tracking-tight sm:text-7xl [font-variation-settings: 'opsz'_144]">
                {symbol ?? '-'}
            </h1>
            <div className="pb-1">
                <p className="font-mono text-3xl tabular-nums">{last ? formatPrice(last.close) : '-'}</p>
                <p className="mt-1 flex items-center gap-2 text-sm text-fg-2">
                    {change !== null && (
                        <span className={
                            change >= 0 
                            ? 'rounded-full bg-accent px-2 py-0.5 font-mono text-accent-ink tabular-nums'
                            : 'rounded-full border border-line px-2 py-0.5 font-mono tabular-nums'
                        }
                        >
                            {formatPercent(change)}
                        </span>
                    )}
                    {last && <span>Last candle {formatTime(last.windowStartMillis)}</span>}
                </p>
            </div>
        </div>
    )
}