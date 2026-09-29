import assert from 'node:assert/strict'
import test from 'node:test'
import { startMapPolling } from './mapPolling.js'

const flush = async () => { await Promise.resolve(); await Promise.resolve() }

test('refreshes the entire map on the server interval and removes expired markers', async (t) => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  const responses = [
    { occurrences: [{ id: 1 }, { id: 2 }], refreshAfterMillis: 1000 },
    { occurrences: [{ id: 1 }], refreshAfterMillis: 1000 },
  ]
  const results = []
  const polling = startMapPolling(async () => responses.shift(), (rows) => results.push(rows), assert.fail)
  t.after(() => polling.stop())
  t.mock.timers.tick(0)
  await flush()
  assert.equal(results[0].length, 2)
  t.mock.timers.tick(999)
  assert.equal(results.length, 1)
  t.mock.timers.tick(1)
  await flush()
  assert.deepEqual(results[1], [{ id: 1 }])
})

test('cancels the initial StrictMode request and never overlaps requests', async (t) => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  let requests = 0
  let finish
  const load = () => { requests++; return new Promise((resolve) => { finish = resolve }) }
  const cancelled = startMapPolling(load, assert.fail, assert.fail)
  cancelled.stop()
  t.mock.timers.tick(0)
  assert.equal(requests, 0)
  const polling = startMapPolling(load, () => {}, assert.fail)
  t.after(() => polling.stop())
  t.mock.timers.tick(0)
  polling.refresh()
  t.mock.timers.tick(10000)
  assert.equal(requests, 1)
  finish({ occurrences: [], refreshAfterMillis: 1000 })
  await flush()
  polling.stop()
  t.mock.timers.tick(10000)
  assert.equal(requests, 1)
})

test('ignores a response after cancellation and aborts the request', async (t) => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  let finish
  let signal
  const polling = startMapPolling((requestSignal) => {
    signal = requestSignal
    return new Promise((resolve) => { finish = resolve })
  }, assert.fail, assert.fail)
  t.mock.timers.tick(0)
  polling.stop()
  assert.equal(signal.aborted, true)
  finish({ occurrences: [{ id: 1 }], refreshAfterMillis: 1000 })
  await flush()
})

test('automatically retries after a connection failure', async (t) => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  let requests = 0
  const errors = []
  const results = []
  const polling = startMapPolling(async () => {
    if (++requests === 1) throw new Error('offline')
    return { occurrences: [], refreshAfterMillis: 1000 }
  }, (rows) => results.push(rows), (error) => errors.push(error.message))
  t.after(() => polling.stop())
  t.mock.timers.tick(0)
  await flush()
  assert.deepEqual(errors, ['offline'])
  t.mock.timers.tick(5000)
  await flush()
  assert.deepEqual(results, [[]])
})
