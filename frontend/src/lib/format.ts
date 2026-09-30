const price = new Intl.NumberFormat(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2});
const percent = new Intl.NumberFormat(undefined, { maximumFractionDigits: 2, signDisplay: 'exceptZero'});


export const formatPrice = (n: number) => price.format(n);
export const formatPercent = (n: number) => `${percent.format(n)}%`;

export const formatTime = (ms: number) => new Date(ms).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit'});
