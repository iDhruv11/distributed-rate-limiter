import { DEMO_TENANT } from './constants'

type ApiResp<T> = { data: T; timestamp: string; requestId: string }

async function call<T>(path: string, opts: RequestInit = {}): Promise<T> {
  const res = await fetch(path, {
    ...opts,
    headers: {
      'Content-Type': 'application/json',
      'X-Tenant-Id': DEMO_TENANT,
      ...(opts.headers || {}),
    },
  })
  const body = await res.json()
  if (!res.ok) {
    const msg = body?.data?.error || body?.error || `request failed (${res.status})`
    throw new Error(msg)
  }
  return (body as ApiResp<T>).data
}

export type Rule = {
  id: string
  tenantId: string
  name: string
  capacity: number
  windowSizeSeconds: number
  keyType: string
  keyValue: string
  createdAt: string
}

export type CheckResult = {
  allowed: boolean
  remainingTokens?: number
  remainingQuota?: number
  refillRate?: number
  reason?: string
  retryAfterSeconds?: number
  initialized?: boolean
}

export type ConcurrencyResult = {
  allowed: number
  denied: number
  tookMs: number
}

export type RedisState = {
  allowed: boolean
  remainingTokens?: number
  refillRate?: number
  windowSeconds?: number
  resetAt?: number
  initialized?: boolean
}

export const api = {
  createRule: (body: object) => call<Rule>('/api/v1/limits', { method: 'POST', body: JSON.stringify(body) }),
  check: (ruleId: string) =>
    call<CheckResult>('/api/v1/limits/check', { method: 'POST', body: JSON.stringify({ ruleId }) }),
  status: (ruleId: string) => call<RedisState>(`/api/v1/limits/${ruleId}/status`),
  reset: (ruleId: string) => call<string>(`/api/v1/rules/${ruleId}/reset`, { method: 'POST', body: '{}' }),
  concurrency: (ruleId: string) => call<ConcurrencyResult>(`/demo/concurrency/${ruleId}`, { method: 'POST', body: '{}' }),
  listRules: () => call<Rule[]>('/demo/rules'),
  updateRule: (ruleId: string, body: object) =>
    call<Rule>(`/api/v1/limits/${ruleId}`, { method: 'PUT', body: JSON.stringify(body) }),
  deleteRule: (ruleId: string) => call<string>(`/api/v1/limits/${ruleId}`, { method: 'DELETE' }),
}
