import { TENANT } from './constants'

async function call<T>(path: string, opts: RequestInit = {}): Promise<T> {
  const res = await fetch(path, {
    ...opts,
    headers: { 'Content-Type': 'application/json', 'X-Tenant-Id': TENANT },
  })
  if (res.status === 204) return undefined as T
  const body = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(body.error || `request failed (${res.status})`)
  return body as T
}

export type Rule = {
  id: string
  name: string
  capacity: number
  windowSeconds: number
  createdAt: string
}

// remaining is null when Redis was down and the service failed open/closed.
export type CheckResult = {
  allowed: boolean
  remaining: number | null
  retryAfterMs: number
  redisDown: boolean
}

export type ConcurrencyResult = {
  requests: number
  allowed: number
  denied: number
  tookMs: number
  p50Ms: number
  p95Ms: number
  p99Ms: number
  maxMs: number
}

type RuleBody = { name: string; capacity: number; windowSeconds: number }

export const api = {
  listRules: () => call<Rule[]>('/api/v1/limits'),
  createRule: (body: RuleBody) => call<Rule>('/api/v1/limits', { method: 'POST', body: JSON.stringify(body) }),
  updateRule: (ruleId: string, body: RuleBody) =>
    call<Rule>(`/api/v1/limits/${ruleId}`, { method: 'PUT', body: JSON.stringify(body) }),
  deleteRule: (ruleId: string) => call<void>(`/api/v1/limits/${ruleId}`, { method: 'DELETE' }),
  check: (ruleId: string, key: string) =>
    call<CheckResult>('/api/v1/limits/check', { method: 'POST', body: JSON.stringify({ ruleId, key }) }),
  reset: (ruleId: string, key: string) =>
    call<void>(`/api/v1/limits/${ruleId}/reset?key=${encodeURIComponent(key)}`, { method: 'POST' }),
  concurrency: (ruleId: string, requests: number) =>
    call<ConcurrencyResult>(`/demo/concurrency/${ruleId}?requests=${requests}`, { method: 'POST' }),
}
