export const DEMO_TENANT = '550e8400-e29b-41d4-a716-446655440000'
export const DEMO_NAME = 'acme-demo'

export const RULE_KEY = 'demo_rule_id'

export function saveRuleId(id: string) {
  localStorage.setItem(RULE_KEY, id)
}

export function loadRuleId(): string {
  return localStorage.getItem(RULE_KEY) || ''
}
