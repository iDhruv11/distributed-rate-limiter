import { useEffect, useState } from 'react'
import { Button } from '../components/ui/button'
import { Card, CardTitle } from '../components/ui/card'
import { Input } from '../components/ui/input'
import { api, Rule } from '../lib/api'
import { saveRuleId } from '../lib/constants'

export default function ManageRulesPage() {
  const [rules, setRules] = useState<Rule[]>([])
  const [editId, setEditId] = useState<string | null>(null)
  const [cap, setCap] = useState('')
  const [win, setWin] = useState('')
  const [err, setErr] = useState('')

  const load = () => api.listRules().then(setRules).catch((e) => setErr(e.message))
  useEffect(() => { load() }, [])

  const act = async (fn: () => Promise<unknown>) => {
    setErr('')
    try {
      await fn()
      load()
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'failed')
    }
  }

  const save = (r: Rule) => act(async () => {
    await api.updateRule(r.id, { name: r.name, capacity: Number(cap), windowSeconds: Number(win) })
    setEditId(null)
  })

  const remove = (r: Rule) => {
    if (confirm(`Delete rule "${r.name}"?`)) act(() => api.deleteRule(r.id))
  }

  return (
    <Card>
      <CardTitle>Manage Rules</CardTitle>
      <p className="mb-4 text-sm text-neutral-500">Only rules you created in this browser.</p>
      {err && <p className="mb-2 text-sm text-red-600">{err}</p>}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b">
              <th className="py-2 pr-4">Name</th>
              <th className="py-2 pr-4">Rule ID</th>
              <th className="py-2 pr-4">Capacity</th>
              <th className="py-2 pr-4">Window (s)</th>
              <th className="py-2">Actions</th>
            </tr>
          </thead>
          <tbody>
            {rules.map((r) => (
              <tr key={r.id} className="border-b">
                <td className="py-3 pr-4">{r.name}</td>
                <td className="py-3 pr-4 font-mono text-xs">
                  <button className="underline" title="Use this rule on the test pages" onClick={() => saveRuleId(r.id)}>
                    {r.id.slice(0, 8)}…
                  </button>
                </td>
                <td className="py-3 pr-4">
                  {editId === r.id ? <Input value={cap} onChange={(e) => setCap(e.target.value)} className="w-20" /> : r.capacity}
                </td>
                <td className="py-3 pr-4">
                  {editId === r.id ? <Input value={win} onChange={(e) => setWin(e.target.value)} className="w-20" /> : r.windowSeconds}
                </td>
                <td className="space-x-2 py-3">
                  {editId === r.id ? (
                    <Button onClick={() => save(r)}>Save</Button>
                  ) : (
                    <Button variant="outline" onClick={() => { setEditId(r.id); setCap(String(r.capacity)); setWin(String(r.windowSeconds)) }}>Edit</Button>
                  )}
                  <Button variant="ghost" onClick={() => remove(r)}>Delete</Button>
                </td>
              </tr>
            ))}
            {rules.length === 0 && (
              <tr><td colSpan={5} className="py-6 text-neutral-500">No rules yet. Create one first.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </Card>
  )
}
