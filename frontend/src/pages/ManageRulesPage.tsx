import { useEffect, useState } from 'react'
import { Button } from '../components/ui/button'
import { Card, CardTitle } from '../components/ui/card'
import { Input } from '../components/ui/input'
import { api, Rule } from '../lib/api'

export default function ManageRulesPage() {
  const [rules, setRules] = useState<Rule[]>([])
  const [editId, setEditId] = useState<string | null>(null)
  const [cap, setCap] = useState('')
  const [win, setWin] = useState('')
  const [err, setErr] = useState('')

  const load = async () => {
    try {
      setRules(await api.listRules())
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'load failed')
    }
  }

  useEffect(() => { load() }, [])

  const startEdit = (r: Rule) => {
    setEditId(r.id)
    setCap(String(r.capacity))
    setWin(String(r.windowSizeSeconds))
  }

  const save = async () => {
    if (!editId) return
    await api.updateRule(editId, { capacity: Number(cap), windowSizeSeconds: Number(win) })
    setEditId(null)
    load()
  }

  const remove = async (id: string) => {
    await api.deleteRule(id)
    load()
  }

  return (
    <Card>
      <CardTitle>Manage Rules</CardTitle>
      <p className="mb-4 text-sm text-neutral-500">acme-demo rules only</p>
      {err && <p className="text-sm text-red-600">{err}</p>}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b">
              <th className="py-2 pr-4">Rule ID</th>
              <th className="py-2 pr-4">Capacity</th>
              <th className="py-2 pr-4">Window</th>
              <th className="py-2">Actions</th>
            </tr>
          </thead>
          <tbody>
            {rules.map((r) => (
              <tr key={r.id} className="border-b">
                <td className="py-3 pr-4 font-mono text-xs">{r.id.slice(0, 8)}…</td>
                <td className="py-3 pr-4">
                  {editId === r.id ? <Input value={cap} onChange={(e) => setCap(e.target.value)} className="w-20" /> : r.capacity}
                </td>
                <td className="py-3 pr-4">
                  {editId === r.id ? <Input value={win} onChange={(e) => setWin(e.target.value)} className="w-20" /> : r.windowSizeSeconds}
                </td>
                <td className="py-3 space-x-2">
                  {editId === r.id ? (
                    <Button onClick={save}>Save</Button>
                  ) : (
                    <Button variant="outline" onClick={() => startEdit(r)}>Edit</Button>
                  )}
                  <Button variant="ghost" onClick={() => remove(r.id)}>Delete</Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  )
}
