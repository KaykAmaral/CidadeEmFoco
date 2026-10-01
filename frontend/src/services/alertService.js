import { ApiError, apiRequest } from './api'

function getActiveAlerts(token) {
  return apiRequest('/api/alerts/active', { token })
}

const RECONNECT_DELAY_MS = 2_000
const MAX_RECONNECT_DELAY_MS = 30_000

function waitForReconnect(delay, signal) {
  return new Promise((resolve) => {
    if (signal.aborted) {
      resolve()
      return
    }

    const timeoutId = window.setTimeout(finish, delay)

    function finish() {
      window.clearTimeout(timeoutId)
      signal.removeEventListener('abort', finish)
      resolve()
    }

    signal.addEventListener('abort', finish, { once: true })
  })
}

async function readAlertEvents(response, onUpdate, signal) {
  const reader = response.body?.getReader()
  if (!reader) throw new Error('O servidor não disponibilizou o fluxo de alertas.')

  const decoder = new TextDecoder()
  let buffer = ''
  let eventName = ''
  let data = []

  function dispatchEvent() {
    if (eventName === 'alerts-updated' && data.join('\n').trim() === 'refresh') {
      onUpdate()
    }
    eventName = ''
    data = []
  }

  function processLine(line) {
    if (line === '') {
      dispatchEvent()
      return
    }
    if (line.startsWith(':')) return

    const separator = line.indexOf(':')
    const field = separator === -1 ? line : line.slice(0, separator)
    const value = separator === -1 ? '' : line.slice(separator + 1).replace(/^ /, '')
    if (field === 'event') eventName = value
    if (field === 'data') data.push(value)
  }

  try {
    while (!signal.aborted) {
      const { done, value } = await reader.read()
      if (done) return

      buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')
      const lines = buffer.split('\n')
      buffer = lines.pop() ?? ''
      lines.forEach(processLine)
    }
  } finally {
    await reader.cancel().catch(() => {})
    reader.releaseLock()
  }
}

async function streamAlertUpdates(token, { signal, onUpdate, onUnauthorized }) {
  const apiUrl = import.meta.env.VITE_API_URL
  let reconnectDelay = RECONNECT_DELAY_MS

  while (!signal.aborted) {
    try {
      const response = await fetch(`${apiUrl}/api/alerts/stream`, {
        headers: {
          Accept: 'text/event-stream',
          Authorization: `Bearer ${token}`,
        },
        signal,
      })

      if (response.status === 401) {
        onUnauthorized()
        return
      }
      if (!response.ok) {
        throw new ApiError('Não foi possível conectar às atualizações dos alertas.', response.status)
      }

      reconnectDelay = RECONNECT_DELAY_MS
      await readAlertEvents(response, onUpdate, signal)
    } catch (error) {
      if (signal.aborted || error.name === 'AbortError') return
      if (error.status === 401) {
        onUnauthorized()
        return
      }
    }

    await waitForReconnect(reconnectDelay, signal)
    reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY_MS)
  }
}

export { getActiveAlerts, streamAlertUpdates }
