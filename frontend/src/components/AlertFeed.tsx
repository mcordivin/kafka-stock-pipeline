import { formatPercent, formatPrice, formatTime } from "../lib/format";
import type { PriceAlert } from '../types';

export function AlertFeed({alerts} : {alerts: PriceAlert[]}) {
    return (
        <section aria-labelledby="alerts-heading" className="flex min-h-0 flex-col">
            <h2 id="alerts-heading" className="font-display text-2xl font-semibold">
                Price alerts
            </h2>
            {alerts.length === 0 ? (
                <p className="mt-3 text-sm text-fg-2">
                    Sharp moves on the selected show up here as they happen.
                </p>
            ) : (
                <ol className="mt-3 divide-y divide-line overflow-y-auto">
                    {alerts.map((a) => (
                        <li key={`${a.symbol}-${a.windowStartMillis}`} className="flex items-baseline gap-3 py-3">
                            <span className="font-mono text-sm font-medium">{a.symbol}</span>
                            <AlertPercent percent={a.percentChange} />
                            <span className="ml-auto text-right text-sm text-fg-2">
                                <span className="font-mono tabular-nums">{formatPrice(a.close)}</span>
                                <span className="block text-xs text-fg-3">{formatTime(a.windowStartMillis)}</span>
                            </span>   
                        </li>
                    ))}
                </ol>
            )}
        </section>
    )
}

export function AlertPercent({ percent } : { percent: number}) {
    return (
        <span className={`rounded-full px-2 py-0.5 font-mono text-xs tabular-nums ${
            percent >= 0 ? 'bg-accent text-accent-ink' : 'bg-primary text-on-primary'
        }`}>
            {formatPercent(percent)}
        </span>
    )
}