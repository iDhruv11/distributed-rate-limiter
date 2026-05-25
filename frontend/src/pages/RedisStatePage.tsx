import { useState } from 'react'
import { Button } from '../components/ui/button'
import { Card, CardTitle, Label } from '../components/ui/card'
import { Input } from '../components/ui/input'
import RedisBadge from '../components/RedisBadge'
import { api, RedisState } from '../lib/api'
import { loadRuleId, saveRuleId } from '../lib/constants'

function na(v: unknown) {
  if (v === null || v === undefined) return 'n/a'
  return v
}

function toDisplay(state: RedisState) {
  const redisDown =
    state.initialized === false &&
    (state.reason === 'redis_unavailable' || state.allowed === true)

  if (redisDown) {
    return {
      allowed: state.allowed,
      remainingQuota: 'n/a',
      resetAt: 'n/a',
      windowSeconds: 'n/a',
      reason: 'n/a',
      retryAfterSeconds: state.retryAfterSeconds ?? null,
      initialized: 'n/a',
      remainingTokens: 'n/a',
      refillRate: 'n/a',
    }
  }

  return {
    allowed: state.allowed,
    remainingQuota: na(state.remainingTokens ?? state.remainingQuota),
    resetAt: na(state.resetAt),
    windowSeconds: na(state.windowSeconds),
    reason: na(state.reason),
    retryAfterSeconds: state.retryAfterSeconds ?? null,
    initialized: na(state.initialized),
    remainingTokens: na(state.remainingTokens ?? state.remainingQuota),
    refillRate: na(state.refillRate),
  }
}



export default function RedisStatePage() {
  const [ruleId, setRuleId] = useState(loadRuleId())
  const [state, setState] = useState<RedisState | null>(null)
  const [err, setErr] = useState('')

  const load = async () => {
    setErr('')
    saveRuleId(ruleId)
    try {
      setState(await api.status(ruleId))
    } catch (e) {
      setState(null)
      setErr(e instanceof Error ? e.message : 'no state yet')
    }
  }

  return (
    <Card>
       <div className="flex items-center justify-between gap-4">
        <CardTitle>Redis State</CardTitle>
        <RedisBadge />
      </div>

      <div className="mt-4 max-w-md space-y-4">
        <div>
          <Label>Rule ID</Label>
          <Input value={ruleId} onChange={(e) => setRuleId(e.target.value)} className={err ? 'border-red-500' : ''} />
          {err && <p className="mt-1 text-sm text-red-600">{err}</p>}
        </div>
        <Button variant="outline" onClick={load}>View Current State</Button>
      </div>
      {state && (
        <pre className="mt-6 overflow-auto rounded-xl border bg-neutral-50 p-4 text-xs">
          {JSON.stringify(toDisplay(state), null, 2)}
        </pre>

      )}
    </Card>
  )
}
