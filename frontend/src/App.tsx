import { Route, Routes, Link } from 'react-router-dom'
import CreateRulePage from './pages/CreateRulePage'
import LiveTestPage from './pages/LiveTestPage'

export default function App() {
  return (
    <div className="min-h-screen bg-white">
      <nav className="flex gap-2 p-4 border-b">
        <Link to="/">Create</Link>
        <Link to="/live">Live Test</Link>
      </nav>
      <main className="p-6">
        <Routes>
          <Route path="/" element={<CreateRulePage />} />
          <Route path="/live" element={<LiveTestPage />} />
        </Routes>
      </main>
    </div>
  )
}
