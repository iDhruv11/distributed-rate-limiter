// Every visitor gets their own random tenant, so people trying the demo can't see or delete each other's rules.
function load(name: string, make: () => string) {
  try {
    const saved = localStorage.getItem(name)
    if (saved) return saved
    const value = make()
    localStorage.setItem(name, value)
    return value
  } catch {
    return make()
  }
}

// crypto.randomUUID only exists on https/localhost, so build one by hand otherwise.
function uuid() {
  if (window.isSecureContext) return crypto.randomUUID()
  return '10000000-1000-4000-8000-100000000000'.replace(/[018]/g, (c) =>
    (Number(c) ^ (Math.random() * 16) >> (Number(c) / 4)).toString(16))
}

export const TENANT = load('demo_tenant', uuid)

export function saveRuleId(id: string) {
  try { localStorage.setItem('demo_rule_id', id) } catch { /* private mode */ }
}

export function loadRuleId(): string {
  try { return localStorage.getItem('demo_rule_id') || '' } catch { return '' }
}
