import { useState } from 'react'
import { Button } from '../components/ui/button'
import { Card, CardTitle, Label } from '../components/ui/card'
import { Input } from '../components/ui/input'
import { api, CheckResult } from '../lib/api'
import { loadRuleId } from '../lib/constants'
import RulePicker from '../components/RulePicker'

export default function LiveTestPage() {
  const [ruleId, setRuleId] = useState(loadRuleId())
  const [key, setKey] = useState('user:1')
  const [results, setResults] = useState<CheckResult[]>([])
  const [err, setErr] = useState('')

  // The 10 requests go out at the same time, not one after another.
  const run = async (count: number) => {
    setErr('')
    try {
      setResults(await Promise.all(Array.from({ length: count }, () => api.check(ruleId, key))))
    } catch (e) {
      setResults([])
      setErr(e instanceof Error ? e.message : 'failed')
    }
  }

  const reset = async () => {
    setErr('')
    try {
      await api.reset(ruleId, key)
      setResults([])
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'reset failed')
    }
  }

  const allowed = results.filter((r) => r.allowed).length
  const lowest = results.reduce<number | null>((min, r) => (r.remaining === null ? min : min === null ? r.remaining : Math.min(min, r.remaining)), null)
  const retry = Math.max(0, ...results.map((r) => r.retryAfterMs))

  return (
    <Card>
      <div className="flex items-center justify-between gap-4">
        <CardTitle>Live Test</CardTitle>
      </div>
      <div className="mt-4 max-w-md space-y-4">
        <RulePicker value={ruleId} onChange={setRuleId} />
        <div>
          <Label>Key (who is being limited)</Label>
          <Input value={key} onChange={(e) => setKey(e.target.value)} />
        </div>
        {err && <p className="text-sm text-red-600">{err}</p>}
        <div className="flex flex-wrap gap-2">
          <Button onClick={() => run(1)}>Send 1 Request</Button>
          <Button variant="outline" onClick={() => run(10)}>Send 10 At Once</Button>
          <Button variant="ghost" onClick={reset}>Reset Bucket</Button>
        </div>
      </div>
      {results.length > 0 && (
        <div className="mt-8 rounded-xl border border-neutral-200 p-4 font-mono text-sm">
          <p>Allowed: {allowed} of {results.length}</p>
          <p>Tokens left: {lowest ?? 'n/a'}</p>
          {retry > 0 && <p>Retry after: {retry} ms</p>}
          {results.some((r) => r.redisDown) && <p className="text-red-600">Redis is down, answered by the fail-open/closed fallback</p>}
        </div>
      )}
    </Card>
  )
}
