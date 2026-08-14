import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Label } from './ui/card'
import { api, Rule } from '../lib/api'
import { saveRuleId } from '../lib/constants'

// Dropdown of this browser's rules, used by the test pages.
export default function RulePicker({ value, onChange }: { value: string; onChange: (id: string) => void }) {
  const [rules, setRules] = useState<Rule[] | null>(null)

  const pick = (id: string) => {
    saveRuleId(id)
    onChange(id)
  }

  useEffect(() => {
    api.listRules()
      .then((list) => {
        setRules(list)
        // Nothing picked yet (or it was deleted): use the newest rule.
        if (list.length > 0 && !list.some((r) => r.id === value)) pick(list[list.length - 1].id)
      })
      .catch(() => setRules([]))
  }, [])

  if (rules === null) return null
  if (rules.length === 0) {
    return <p className="text-sm text-neutral-600">No rules yet. <Link to="/" className="underline">Create one first.</Link></p>
  }

  return (
    <div>
      <Label>Rule</Label>
      <select
        className="flex h-10 w-full rounded-xl border border-neutral-300 bg-white px-3 text-sm"
        value={value}
        onChange={(e) => pick(e.target.value)}
      >
        {rules.map((r) => (
          <option key={r.id} value={r.id}>{r.name}: {r.capacity} per {r.windowSeconds}s</option>
        ))}
      </select>
    </div>
  )
}
