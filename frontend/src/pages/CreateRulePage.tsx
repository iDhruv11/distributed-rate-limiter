import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '../components/ui/button'
import { Card, CardTitle, Label } from '../components/ui/card'
import { Input } from '../components/ui/input'
import { api, Rule } from '../lib/api'
import { DEMO_NAME, saveRuleId } from '../lib/constants'

const KEY_TYPES = [
  { value: 'email', label: 'email' },
  { value: 'int_id', label: 'int id' },
  { value: 'api_key', label: 'api key' },
  { value: 'ip_address', label: 'ip address' },
]

export default function CreateRulePage() {
  const nav = useNavigate()
  const [capacity, setCapacity] = useState('100')
  const [windowSec, setWindowSec] = useState('60')
  const [keyType, setKeyType] = useState('email')
  const [keyValue, setKeyValue] = useState('')
  const [created, setCreated] = useState<Rule | null>(null)
  const [err, setErr] = useState('')

  const submit = async () => {
    setErr('')
    try {
      const rule = await api.createRule({
        name: DEMO_NAME,
        capacity: Number(capacity),
        windowSizeSeconds: Number(windowSec),
        keyType,
        keyValue: keyValue || 'demo-value',
      })
      saveRuleId(rule.id)
      setCreated(rule)
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'failed')
    }
  }

  return (
    <Card>
      <CardTitle>Create Rule</CardTitle>
      <p className="mb-6 text-sm text-neutral-500">Demo tenant locked to acme-demo</p>
      <div className="grid max-w-md gap-4">
        <div>
          <Label>Name</Label>
          <Input value={DEMO_NAME} readOnly className="bg-neutral-50" />
        </div>
        <div>
          <Label>Capacity</Label>
          <Input value={capacity} onChange={(e) => setCapacity(e.target.value)} type="number" />
        </div>
        <div>
          <Label>Window Seconds</Label>
          <Input value={windowSec} onChange={(e) => setWindowSec(e.target.value)} type="number" />
        </div>
        <div>
          <Label>Key Type</Label>
          <select
            className="flex h-10 w-full rounded-xl border border-neutral-300 px-3 text-sm"
            value={keyType}
            onChange={(e) => setKeyType(e.target.value)}
          >
            {KEY_TYPES.map((t) => (
              <option key={t.value} value={t.value}>{t.label}</option>
            ))}
          </select>
        </div>
        <div>
          <Label>Key Value</Label>
          <Input value={keyValue} onChange={(e) => setKeyValue(e.target.value)} placeholder="any value" />
        </div>
        {err && <p className="text-sm text-red-600">{err}</p>}
        <Button onClick={submit}>Create Rule</Button>
      </div>
      {created && (
        <div className="mt-8 rounded-xl border border-neutral-200 bg-neutral-50 p-4 text-sm">
          <p className="font-medium">Rule added</p>
          <pre className="mt-2 overflow-auto text-xs">{JSON.stringify({
            ruleId: created.id,
            tenantId: created.tenantId,
            capacity: created.capacity,
            window: created.windowSizeSeconds,
            keyType: created.keyType,
            keyValue: created.keyValue,
            createdAt: created.createdAt,
          }, null, 2)}</pre>
          <Button className="mt-4" onClick={() => nav('/live')}>Proceed To Testing →</Button>
        </div>
      )}
    </Card>
  )
}
