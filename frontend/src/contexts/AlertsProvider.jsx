import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import useAuth from '../hooks/useAuth'
import { getActiveAlerts, listenForAlertUpdates } from '../services/alertService'
import AlertsContext from './alertsContext'

const INITIAL_RECONNECT_DELAY_MS = 3_000
const MAX_RECONNECT_DELAY_MS = 30_000

function uniqueAlerts(alerts) {
  return [...new Map(alerts.map((alert) => [alert.id, alert])).values()]
}

function AlertsProvider({ children }) {
  const { logout, token } = useAuth()
  const [alerts, setAlerts] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const refreshPromiseRef = useRef(null)

  const refresh = useCallback(async ({ showLoading = false } = {}) => {
    if (refreshPromiseRef.current) return refreshPromiseRef.current
    if (showLoading) setIsLoading(true)

    const request = getActiveAlerts(token)
      .then((data) => {
        setAlerts(uniqueAlerts(data))
        setError('')
      })
      .catch((requestError) => {
        if (requestError.status === 401) {
          logout()
          return
        }
        if (showLoading) setError(requestError.message)
      })
      .finally(() => {
        refreshPromiseRef.current = null
        if (showLoading) setIsLoading(false)
      })

    refreshPromiseRef.current = request
    return request
  }, [logout, token])

  useEffect(() => {
    const controller = new AbortController()
    let reconnectTimer = null
    let reconnectDelay = INITIAL_RECONNECT_DELAY_MS

    async function connect() {
      try {
        await listenForAlertUpdates(token, {
          onUpdate: () => refresh(),
          signal: controller.signal,
        })
        reconnectDelay = INITIAL_RECONNECT_DELAY_MS
      } catch (requestError) {
        if (controller.signal.aborted) return
        if (requestError.status === 401) {
          logout()
          return
        }
      }

      if (controller.signal.aborted) return
      await refresh()
      reconnectTimer = window.setTimeout(connect, reconnectDelay)
      reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY_MS)
    }

    window.queueMicrotask(() => {
      if (controller.signal.aborted) return
      refresh({ showLoading: true }).finally(() => {
        if (!controller.signal.aborted) connect()
      })
    })

    return () => {
      controller.abort()
      if (reconnectTimer) window.clearTimeout(reconnectTimer)
    }
  }, [logout, refresh, token])

  const value = useMemo(() => ({
    alerts,
    error,
    isLoading,
    retry: () => refresh({ showLoading: true }),
  }), [alerts, error, isLoading, refresh])

  return <AlertsContext.Provider value={value}>{children}</AlertsContext.Provider>
}

export default AlertsProvider
