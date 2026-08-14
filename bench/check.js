// Load test for the check endpoint with k6 (https://k6.io).
// Sends RATE checks per second for 30 seconds, no matter how slow the answers are, so slow answers can't hide queueing.
//
//   docker compose up -d
//   docker run --rm --network host -v "$PWD/bench:/b" -e RATE=1000 grafana/k6:0.54.0 run /b/check.js
//
// Point it somewhere else with -e BASE=http://host:port.
import http from 'k6/http';

const BASE = __ENV.BASE || 'http://127.0.0.1:8082';
const RATE = Number(__ENV.RATE || 1000);

export const options = {
  discardResponseBodies: true,
  summaryTrendStats: ['p(50)', 'p(95)', 'p(99)', 'max'],
  scenarios: {
    fixed_rate: { executor: 'constant-arrival-rate', rate: RATE, timeUnit: '1s', duration: '30s', preAllocatedVUs: 300, maxVUs: 600 },
  },
};

function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-8xxx-xxxxxxxxxxxx'.replace(/x/g, () => Math.floor(Math.random() * 16).toString(16));
}

// Make a throwaway tenant and a rule big enough that nothing gets denied.
export function setup() {
  const headers = { 'Content-Type': 'application/json', 'X-Tenant-Id': uuid() };
  const res = http.post(`${BASE}/api/v1/limits`, JSON.stringify({ name: 'bench', capacity: 1000000, windowSeconds: 60 }),
    { headers, responseType: 'text' });
  return { headers, ruleId: res.json('id') };
}

// Each check is for one of 1,000 users, like real traffic.
export default function (d) {
  const key = `user:${Math.floor(Math.random() * 1000)}`;
  http.post(`${BASE}/api/v1/limits/check`, JSON.stringify({ ruleId: d.ruleId, key }), { headers: d.headers });
}

export function teardown(d) {
  http.del(`${BASE}/api/v1/limits/${d.ruleId}`, null, { headers: d.headers });
}
