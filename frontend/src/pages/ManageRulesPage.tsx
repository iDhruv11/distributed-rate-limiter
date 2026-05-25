import { useEffect, useState } from 'react'
import { Button } from '../components/ui/button'
import { Card, CardTitle } from '../components/ui/card'
import { Input } from '../components/ui/input'
import RedisBadge from '../components/RedisBadge'

import { api, Rule } from '../lib/api'

function isRedisErr(msg: string) {
  return msg.toLowerCase().includes('redis') || msg.toLowerCase().includes('unavailable') || msg.toLowerCase().includes('503')
}


export default function ManageRulesPage() {
  const [rules, setRules] = useState<Rule[]>([])
  const [editId, setEditId] = useState<string | null>(null)
  const [cap, setCap] = useState('')
  const [win, setWin] = useState('')
  const [err, setErr] = useState('')
  
  const [redisErr, setRedisErr] = useState('')


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
    setRedisErr('')

  }

  const save = async () => {
    if (!editId) return
    setRedisErr('')
    try {
      await api.updateRule(editId, { capacity: Number(cap), windowSizeSeconds: Number(win) })
      setEditId(null)
      load()
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'save failed'
      if (isRedisErr(msg)) {
        setRedisErr('one or more resources are unreachable')
      } else {
        setErr(msg)
      }
    }
  }

  const remove = async (id: string) => {
    setRedisErr('')
    try {
      await api.deleteRule(id)
      load()
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'delete failed'
      if (isRedisErr(msg)) {
        setRedisErr('one or more resources are unreachable')
      } else {
        setErr(msg)
      }
    }
  }

  return (
    <Card>
      <div className="flex items-center justify-between gap-4">
        <CardTitle>Manage Rules</CardTitle>
        <RedisBadge />
      </div>

      <p className="mb-4 text-sm text-neutral-500">acme-demo rules only</p>
      {err && <p className="text-sm text-red-600">{err}</p>}
      {redisErr && <p className="mb-2 text-sm text-red-600">{redisErr}</p>}
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
