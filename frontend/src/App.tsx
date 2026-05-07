import { Link, Route, Routes, useLocation } from 'react-router-dom'
import CreateRulePage from './pages/CreateRulePage'
import LiveTestPage from './pages/LiveTestPage'
import ConcurrencyPage from './pages/ConcurrencyPage'
import RedisStatePage from './pages/RedisStatePage'
import ManageRulesPage from './pages/ManageRulesPage'

const nav = [
  { path: '/', label: 'Create' },
  { path: '/live', label: 'Live Test' },
  { path: '/concurrency', label: 'Concurrency' },
  { path: '/state', label: 'Redis State' },
  { path: '/manage', label: 'Manage' },
]

export default function App() {
  const loc = useLocation()

  return (
    <div className="min-h-screen bg-white">
      <header className="border-b border-neutral-200">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-5">
          <div>
            <h1 className="text-xl font-semibold tracking-tight">Rate Limiter</h1>
            <p className="text-sm text-neutral-500">Distributed token bucket demo</p>
          </div>
        </div>
        <nav className="mx-auto flex max-w-5xl gap-1 px-6 pb-4">
          {nav.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`rounded-lg px-3 py-1.5 text-sm ${loc.pathname === item.path ? 'bg-black text-white' : 'text-neutral-600 hover:bg-neutral-100'}`}
            >
              {item.label}
            </Link>
          ))}
        </nav>
      </header>
      <main className="mx-auto max-w-5xl px-6 py-8">
        <Routes>
          <Route path="/" element={<CreateRulePage />} />
          <Route path="/live" element={<LiveTestPage />} />
          <Route path="/concurrency" element={<ConcurrencyPage />} />
          <Route path="/state" element={<RedisStatePage />} />
          <Route path="/manage" element={<ManageRulesPage />} />
        </Routes>
      </main>
    </div>
  )
}
