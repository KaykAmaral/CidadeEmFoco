import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

test('alert illustrations support responsive and reduced-motion layouts', () => {
  const illustration = readFileSync(new URL('../components/alerts/CalmCityIllustration.jsx', import.meta.url), 'utf8')
  const styles = readFileSync(new URL('../components/alerts/CalmCityIllustration.css', import.meta.url), 'utf8')
  const alertStyles = readFileSync(new URL('../components/alerts/ClimateAlertCard.css', import.meta.url), 'utf8')

  assert.match(illustration, /aria-label=/)
  assert.match(styles, /calm-city--day|calm-city__sky/)
  assert.match(styles, /calm-city--afternoon/)
  assert.match(styles, /calm-city--night/)
  assert.match(`${styles}\n${alertStyles}`, /prefers-reduced-motion:\s*reduce/)
})

test('admin layout shows only city data supplied by the authenticated user', () => {
  const layout = readFileSync(new URL('../layouts/AdminLayout.jsx', import.meta.url), 'utf8')
  assert.match(layout, /user\.city\.name/)
  assert.match(layout, /user\.city\.stateCode/)
  assert.match(layout, /Não informada pela API/)
  assert.doesNotMatch(layout, /Praia Grande/)
})
