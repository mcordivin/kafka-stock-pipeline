import type { Connection } from '../hooks/useLiveFeed';

export function ConnectionStatus({ connection }: {connection: Connection}) {
    const label = 
    connection.status === 'open'
    ? 'Live'
    : connection.status === 'connecting'
      ? 'Connecting…'
      : `Reconnecting in ${connection.inSeconds}s`;

      return (
        <span role="status" className="flex items-center gap-2 text-sm text-fg-2">
            <span aria-hidden
            className={`size-2 roudned-full ${connection.status === 'open' ?
                'bg-accent ring-2 ring-accent-ink/30' : 'bg-fg-3'
            }`}
            />
            {label}
        </span>
      )
}