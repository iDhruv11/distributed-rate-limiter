import { useState } from 'react'
import { Button } from '../components/ui/button'
import { Card, CardTitle, Label } from '../components/ui/card'
import RulePicker from '../components/RulePicker'
import { api, ConcurrencyResult } from '../lib/api'
import { loadRuleId } from '../lib/constants'

const SIZES = [100, 200, 500]

function Stat({ value, label }: { value: string | number; label: string }) {
  return (
    <div className="rounded-xl border p-4">
      <p className="text-2xl font-bold">{value}</p>
      <p className="text-xs uppercase text-neutral-500">{label}</p>
    </div>
  )
}

export default function ConcurrencyPage() {
  const [ruleId, setRuleId] = useState(loadRuleId())
  const [size, setSize] = useState(100)
  const [result, setResult] = useState<ConcurrencyResult | null>(null)
  const [err, setErr] = useState('')
  const [loading, setLoading] = useState(false)

  const run = async () => {
    setErr('')
    setLoading(true)
    try {
      setResult(await api.concurrency(ruleId, size))
    } catch (e) {
      setResult(null)
      setErr(e instanceof Error ? e.message : 'failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="border-2 border-black">
      <CardTitle>Concurrency Demo</CardTitle>
      <p className="mb-4 text-sm text-neutral-600">
        Fills the bucket, then fires all the checks at it at the same moment. With capacity N, N get through,
        plus any tokens that refill during the run (none for a rule like 10 per minute).
      </p>
      <div className="max-w-md space-y-4">
        <RulePicker value={ruleId} onChange={setRuleId} />
        <div>
          <Label>Requests at once</Label>
          <div className="flex gap-2">
            {SIZES.map((n) => (
              <Button key={n} variant={size === n ? 'default' : 'outline'} onClick={() => setSize(n)}>{n}</Button>
            ))}
          </div>
        </div>
        {err && <p className="text-sm text-red-600">{err}</p>}
        <Button onClick={run} disabled={loading || !ruleId}>{loading ? 'Running...' : `Run ${size} Concurrent Requests`}</Button>
      </div>
      {result && (
        <div className="mt-8 space-y-4 text-center">
          <div className="grid grid-cols-3 gap-4">
            <Stat value={result.allowed} label="Allowed" />
            <Stat value={result.denied} label="Denied" />
            <Stat value={`${result.tookMs} ms`} label="Whole burst" />
          </div>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <Stat value={`${result.p50Ms} ms`} label="p50" />
            <Stat value={`${result.p95Ms} ms`} label="p95" />
            <Stat value={`${result.p99Ms} ms`} label="p99" />
            <Stat value={`${result.maxMs} ms`} label="Slowest" />
          </div>
          <p className="text-left text-xs text-neutral-500">
            Time each single check took inside the server during this burst ({result.requests} samples), so it
            doesn't include your network. With only {result.requests} samples, p99 is close to the slowest one.
          </p>
        </div>
      )}
    </Card>
  )
}
