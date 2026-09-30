import {useEffect, useRef, useState} from 'react';
import {WS_URL, parseServerMessage} from '../lib/api';
import type { ServerMessage} from '../types';

export type Connection = 
    | { status: 'connecting' }
    | { status: 'open' }
    | { status: 'reconnecting'; inSeconds: number };

const MAX_DELAY_S = 30;

function subscribe(ws: WebSocket, symbol: string) {
    ws.send(JSON.stringify({ action: 'subscribe', symbol }));
}

export function useLiveFeed(symbol: string | null, onMessage: (msg: ServerMessage) => void) {
    const [connection, setConnection] = useState<Connection>({ status: 'connecting'});
    const wsRef = useRef<WebSocket | null> (null);
    const symbolRef = useRef(symbol);
    const onMessageRef = useRef(onMessage);

    useEffect(() => {
        onMessageRef.current = onMessage;
    });

    useEffect(() => {
        let disposed = false;
        let attempt = 0;
        let timer: number | undefined;

        const connect = () => {
            const ws = new WebSocket(WS_URL);
            wsRef.current = ws;

            ws.onopen = () => {
                attempt = 0;
                setConnection({ status: 'open'});
                if(symbolRef.current) subscribe(ws, symbolRef.current);
            };

            ws.onmessage = (e) => {
                const msg = parseServerMessage(e.data);
                if(msg) onMessageRef.current(msg);
            };

            ws.onclose = () => {
                if(disposed) return;
                attempt += 1;
                const delay = Math.min(2 ** (attempt - 1), MAX_DELAY_S);
                setConnection({ status: 'reconnecting', inSeconds: delay});
                    timer = window.setTimeout(() => {
                         setConnection({ status: 'connecting' });
                         connect();
                        }, delay * 1000);
            };
        };

        connect();
        return () => {
            disposed = true;
            window.clearTimeout(timer);
            wsRef.current?.close();
        };
    }, []);

    useEffect(() => {
        symbolRef.current = symbol;
        const ws = wsRef.current;
        if(symbol && ws?.readyState === WebSocket.OPEN) subscribe(ws, symbol);
    }, [symbol]);

    return connection;
}