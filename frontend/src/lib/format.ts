export function showVal(v: unknown, suffix = '') {
  if (v === null || v === undefined) return 'n/a'
  if (typeof v === 'number' && suffix) return `${v.toFixed(2)}${suffix}`
  return String(v)
}
