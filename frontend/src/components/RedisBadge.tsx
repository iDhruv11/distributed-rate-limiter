import { useEffect, useState } from 'react'
import { api } from '../lib/api'

export default function RedisBadge() {
  const [up, setUp] = useState<boolean | null>(null)

  useEffect(() => {
    let alive = true
    const check = async () => {
      try {
        const r = await api.redisHealth()
        if (alive) setUp(r.up)
      } catch {
        if (alive) setUp(false)
      }
    }
    check()
    const t = setInterval(check, 5000)
    return () => {
      alive = false
      clearInterval(t)
    }
  }, [])

  if (up === null) return null

  return (
    <span className={`inline-flex items-center gap-2 rounded-full border px-3 py-1 text-xs ${up ? 'border-green-200 bg-green-50 text-green-800' : 'border-red-200 bg-red-50 text-red-800'}`}>
      <span className={`h-2 w-2 rounded-full ${up ? 'bg-green-500' : 'bg-red-500'}`} />
      {up ? 'redis up' : 'redis down'}
    </span>
  )
}
