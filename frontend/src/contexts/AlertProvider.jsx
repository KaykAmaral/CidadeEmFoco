import { useCallback, useEffect, useState } from 'react'
import AlertContext from './alertContext'
import { getActiveAlerts, streamAlertUpdates } from '../services/alertService'

function AlertProvider({ children, logout, token }) {
  const [alerts, setAlerts] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  const refreshAlerts = useCallback(async () => {
    try {
      const activeAlerts = await getActiveAlerts(token)
      setAlerts(activeAlerts)
      setError('')
      return true
    } catch (requestError) {
      if (requestError.status === 401) {
        logout()
        return false
      }
      setError(requestError.message)
      return false
    } finally {
      setIsLoading(false)
    }
  }, [logout, token])

  useEffect(() => {
    const controller = new AbortController()
    refreshAlerts()
    streamAlertUpdates(token, {
      signal: controller.signal,
      onUpdate: refreshAlerts,
      onUnauthorized: logout,
    })

    return () => controller.abort()
  }, [logout, refreshAlerts, token])

  return (
    <AlertContext.Provider value={{ alerts, error, isLoading, refreshAlerts }}>
      {children}
    </AlertContext.Provider>
  )
}

export default AlertProvider
