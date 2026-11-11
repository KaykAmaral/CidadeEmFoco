import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

test('keeps the map before recent occurrences in the mobile document flow', () => {
  const page = readFileSync(new URL('../pages/CitizenHomePage.jsx', import.meta.url), 'utf8')
  const styles = readFileSync(new URL('../pages/CitizenHomePage.css', import.meta.url), 'utf8')

  assert.ok(page.indexOf('dashboard-map-card') < page.indexOf('recent-occurrences'))
  assert.match(styles, /@media \(max-width: 920px\)[\s\S]*?\.dashboard-grid\s*{[\s\S]*?grid-template-columns:\s*1fr/)
})
