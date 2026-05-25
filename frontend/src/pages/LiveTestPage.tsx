import { useState } from 'react'
import { Button } from '../components/ui/button'
import { Card, CardTitle, Label } from '../components/ui/card'
import { Input } from '../components/ui/input'
import RedisBadge from '../components/RedisBadge'
import { api, CheckResult } from '../lib/api'
import { showVal } from '../lib/format'
import { loadRuleId, saveRuleId } from '../lib/constants'

export default function LiveTestPage() {
  const [ruleId, setRuleId] = useState(loadRuleId())
  const [last, setLast] = useState<CheckResult | null>(null)
  const [err, setErr] = useState('')

  const run = async (count: number) => {
    setErr('')
    saveRuleId(ruleId)
    try {
      let result: CheckResult | null = null
      for (let i = 0; i < count; i++) {
        result = await api.check(ruleId)
      }
      setLast(result)
    } catch (e) {
      setLast(null)
      setErr(e instanceof Error ? e.message : 'rule not found')
    }
  }

  const reset = async () => {
    setErr('')
    try {
      await api.reset(ruleId)
      setLast(null)
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'reset failed')
    }
  }

  return (
    <Card>
      <div className="flex items-center justify-between gap-4">
        <CardTitle>Live Rate Limiter Demo</CardTitle>
        <RedisBadge />
      </div>

      <div className="mt-4 max-w-md space-y-4">
        <div>
          <Label>Rule ID</Label>
          <Input value={ruleId} onChange={(e) => setRuleId(e.target.value)} className={err ? 'border-red-500' : ''} />
          {err && <p className="mt-1 text-sm text-red-600">{err}</p>}
        </div>
        <div className="flex flex-wrap gap-2">
          <Button onClick={() => run(1)}>Send 1 Request</Button>
          <Button variant="outline" onClick={() => run(10)}>Send 10 Requests</Button>
          <Button variant="ghost" onClick={reset}>Reset Quota</Button>
        </div>
      </div>
      {last && (
        <div className="mt-8 rounded-xl border border-neutral-200 p-4 font-mono text-sm">
          <p>Allowed: {String(last.allowed)}</p>
          <p>Remaining Tokens: {last.initialized === false ? 'n/a' : showVal(last.remainingTokens ?? last.remainingQuota)}</p>
          <p>Refill Rate: {last.initialized === false ? 'n/a' : showVal(last.refillRate, '/sec')}</p>
        </div>
      )}
    </Card>
  )
}
