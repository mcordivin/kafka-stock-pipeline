import type { Theme } from '../lib/theme';

export function ThemeToggle({ theme, onToggle }: {theme: Theme; onToggle: ()=> void}) {
    return (
        <button 
            type="button"
            onClick={onToggle}
            className="rounded-card border border-line px-3 py-1.5 text-sm text-fg-2 hover:bg-secondary/30 hover:text-fg-1">
                {theme === 'dark' ? 'Light mode' : 'Dark mode'}
            </button>
    )
}