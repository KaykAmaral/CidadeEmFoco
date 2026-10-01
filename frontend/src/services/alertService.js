import { API_URL, ApiError, apiRequest } from './api'

function getActiveAlerts(token) {
  return apiRequest('/api/alerts/active', { token })
}

async function listenForAlertUpdates(token, { onUpdate, signal }) {
  let response

  try {
    response = await fetch(`${API_URL}/api/alerts/stream`, {
      headers: {
        Accept: 'text/event-stream',
        Authorization: `Bearer ${token}`,
      },
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new ApiError('Canal de alertas temporariamente indisponível.')
  }

  if (!response.ok) {
    throw new ApiError(
      response.status === 401
        ? 'Sua sessão expirou.'
        : 'Canal de alertas temporariamente indisponível.',
      response.status,
    )
  }

  if (!response.body) {
    throw new ApiError('O servidor não disponibilizou o canal de alertas.')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let lastEventId = ''

  while (!signal.aborted) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true }).replaceAll('\r\n', '\n')
    const messages = buffer.split('\n\n')
    buffer = messages.pop() ?? ''

    messages.forEach((message) => {
      let eventName = 'message'
      let eventId = ''

      message.split('\n').forEach((line) => {
        if (line.startsWith('event:')) eventName = line.slice(6).trim()
        if (line.startsWith('id:')) eventId = line.slice(3).trim()
      })

      if (eventName === 'alerts-updated' && (!eventId || eventId !== lastEventId)) {
        lastEventId = eventId
        onUpdate()
      }
    })
  }
}

export { getActiveAlerts, listenForAlertUpdates }
