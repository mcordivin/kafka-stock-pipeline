import { useEffect } from "react";
import { AlertPercent } from "./AlertFeed";
import type { PriceAlert } from "../types";

const TTL_MS = 6000;

interface Props {
    toasts: PriceAlert[];
    onDismiss: (alert: PriceAlert) => void;
}

export function AlertToast({ toasts, onDismiss} : Props) {
    return (
        <div aria-live="polite" className="fixed right-bottom-4 z-10 flex w-72 flex-col gap-2">
            {toasts.map((t) => (
                <Toast key={`${t.symbol}-${t.windowStartMillis}`} alert={t} onDismiss={onDismiss} />
            ))}
        </div>
    )
}

export function Toast({ alert, onDismiss} : {alert: PriceAlert, onDismiss: (a: PriceAlert) => void}) {
    useEffect(() => {
        const id = window.setTimeout(() => onDismiss(alert), TTL_MS);
        return () => window.clearTimeout(id);
    }, [alert, onDismiss]);

    return (
        <div className="animate-toast flex items-center gap-3 rounded-card bg-primary px-4 py-3 text-on-primary shadow-lg">
            <span className="font-mono text-sm font-medium">{alert.symbol}</span>
            <AlertPercent percent={alert.percentChange} />
            <button 
                type="button"
                onClick={()=> onDismiss(alert)}
                className="ml-auto text-sm opacity-70 hover:opacity-100"
                aria-label={`Dismiss ${alert.symbol} alert`}>x</button>
        </div>
    )
}