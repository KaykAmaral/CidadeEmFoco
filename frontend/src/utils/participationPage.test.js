import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const appSource = readFileSync(new URL('../App.jsx', import.meta.url), 'utf8')
const layoutSource = readFileSync(new URL('../layouts/CitizenLayout.jsx', import.meta.url), 'utf8')
const profileSource = readFileSync(new URL('../pages/ProfilePage.jsx', import.meta.url), 'utf8')
const gamificationSource = readFileSync(new URL('../components/profile/GamificationPanel.jsx', import.meta.url), 'utf8')

test('exposes participation as a citizen route and active navigation destination', () => {
  assert.match(appSource, /path="participacao"/)
  assert.match(layoutSource, /to: '\/app\/participacao', label: 'Participação'/)
})

test('moves gamification out of profile without duplicating its API-backed panel', () => {
  assert.doesNotMatch(profileSource, /GamificationPanel/)
  assert.match(gamificationSource, /getMyGamification/)
  assert.match(gamificationSource, /getCitizenRanking/)
  assert.match(gamificationSource, /getCityRanking/)
})

test('keeps ranking periods and empty states available', () => {
  assert.match(gamificationSource, /\['WEEK', 'MONTH', 'YEAR', 'ALL'\]/)
  assert.match(gamificationSource, /\['WEEK', 'MONTH', 'YEAR'\]/)
  assert.match(gamificationSource, /Nenhum participante neste período/)
  assert.match(gamificationSource, /Nenhuma prefeitura classificada neste período/)
})
