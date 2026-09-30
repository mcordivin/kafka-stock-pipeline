interface Props {
    symbols: string[];
    value: string | null;
    onChange: (symbol: string) => void;
}

export function SymbolPicker({ symbols, value, onChange }: Props) {
    return (
        <label className="flex items-center gap-2 text-sm text-fg-2">
            Symbol
            <select
                value={value ?? ''}
                onChange={(e) => onChange(e.target.value)}
                disabled={symbols.length === 0}
                className="rounded-card border border-line bg-surface px-3 py-1.5 font-momo text-fg-1 hover:border-secondary-hov disabled:opacity-50"
            >
                {symbols.map((s) => (
                    <option key={s} value={s}>{s}</option>
                ))}
            </select>
        </label>
    )
}