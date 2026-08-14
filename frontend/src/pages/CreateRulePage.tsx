import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '../components/ui/button'
import { Card, CardTitle, Label } from '../components/ui/card'
import { Input } from '../components/ui/input'
import { api, Rule } from '../lib/api'
import { saveRuleId } from '../lib/constants'

export default function CreateRulePage() {
  const nav = useNavigate()
  const [name, setName] = useState('login')
  const [capacity, setCapacity] = useState('10')
  const [windowSec, setWindowSec] = useState('60')
  const [created, setCreated] = useState<Rule | null>(null)
  const [err, setErr] = useState('')

  const create = async (body: { name: string; capacity: number; windowSeconds: number }) => {
    setErr('')
    try {
      const rule = await api.createRule(body)
      saveRuleId(rule.id)
      setCreated(rule)
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'failed')
    }
  }

  return (
    <Card>
      <CardTitle>Create Rule</CardTitle>
      <p className="mb-6 text-sm text-neutral-500">
        A rule is a limit like "10 requests per 60 seconds". Each user/IP/API key you check against it gets its own bucket.
      </p>
      <div className="grid max-w-md gap-4">
        <div>
          <Label>Name</Label>
          <Input value={name} onChange={(e) => setName(e.target.value)} />
        </div>
        <div>
          <Label>Capacity (requests)</Label>
          <Input value={capacity} onChange={(e) => setCapacity(e.target.value)} type="number" />
        </div>
        <div>
          <Label>Window (seconds)</Label>
          <Input value={windowSec} onChange={(e) => setWindowSec(e.target.value)} type="number" />
        </div>
        {err && <p className="text-sm text-red-600">{err}</p>}
        <Button onClick={() => create({ name, capacity: Number(capacity), windowSeconds: Number(windowSec) })}>Create Rule</Button>
        <div>
          <Label>Or one click</Label>
          <div className="flex flex-wrap gap-2">
            {[10, 60, 100, 200].map((n) => (
              <Button key={n} variant="outline" onClick={() => create({ name: `${n}-per-minute`, capacity: n, windowSeconds: 60 })}>
                {n} / min
              </Button>
            ))}
          </div>
        </div>
      </div>
      {created && (
        <div className="mt-8 rounded-xl border border-neutral-200 bg-neutral-50 p-4 text-sm">
          <p className="font-medium">Rule added</p>
          <pre className="mt-2 overflow-auto text-xs">{JSON.stringify(created, null, 2)}</pre>
          <Button className="mt-4" onClick={() => nav('/live')}>Proceed To Testing →</Button>
        </div>
      )}
    </Card>
  )
}
