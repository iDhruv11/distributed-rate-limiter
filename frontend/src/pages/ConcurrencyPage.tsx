import { useState } from 'react'
import { Button } from '../components/ui/button'
import { Card, CardTitle, Label } from '../components/ui/card'
import { Input } from '../components/ui/input'
import { api, ConcurrencyResult } from '../lib/api'
import { loadRuleId, saveRuleId } from '../lib/constants'

export default function ConcurrencyPage() {
  const [ruleId, setRuleId] = useState(loadRuleId())
  const [result, setResult] = useState<ConcurrencyResult | null>(null)
  const [err, setErr] = useState('')
  const [loading, setLoading] = useState(false)

  const run = async () => {
    setErr('')
    setLoading(true)
    saveRuleId(ruleId)
    try {
      const r = await api.concurrency(ruleId)
      setResult(r)
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
      <p className="mb-4 text-sm text-neutral-600">100 threads · same rule · atomic Lua</p>
      <div className="max-w-md space-y-4">
        <div>
          <Label>Rule ID</Label>
          <Input value={ruleId} onChange={(e) => setRuleId(e.target.value)} className={err ? 'border-red-500' : ''} />
          {err && <p className="mt-1 text-sm text-red-600">{err}</p>}
        </div>
        <Button onClick={run} disabled={loading}>{loading ? 'Running...' : 'Run 100 Concurrent Requests'}</Button>
      </div>
      {result && (
        <div className="mt-8 grid grid-cols-2 gap-4 text-center">
          <div className="rounded-xl border p-4">
            <p className="text-2xl font-bold">{result.allowed}</p>
            <p className="text-xs uppercase text-neutral-500">Allowed</p>
          </div>
          <div className="rounded-xl border p-4">
            <p className="text-2xl font-bold">{result.denied}</p>
            <p className="text-xs uppercase text-neutral-500">Denied</p>
          </div>
        </div>
      )}
    </Card>
  )
}
