import { useState } from 'react'
import { Button } from '../components/ui/button'
import { Card, CardTitle, Label } from '../components/ui/card'
import { Input } from '../components/ui/input'
import { api, RedisState } from '../lib/api'
import { loadRuleId, saveRuleId } from '../lib/constants'

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
      <CardTitle>Redis State</CardTitle>
      <div className="mt-4 max-w-md space-y-4">
        <div>
          <Label>Rule ID</Label>
          <Input value={ruleId} onChange={(e) => setRuleId(e.target.value)} className={err ? 'border-red-500' : ''} />
          {err && <p className="mt-1 text-sm text-red-600">{err}</p>}
        </div>
        <Button variant="outline" onClick={load}>View Current State</Button>
      </div>
      {state && (
        <pre className="mt-6 overflow-auto rounded-xl border bg-neutral-50 p-4 text-xs">{JSON.stringify(state, null, 2)}</pre>
      )}
    </Card>
  )
}
