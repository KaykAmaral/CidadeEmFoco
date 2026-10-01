import { useContext } from 'react'
import AlertsContext from '../contexts/alertsContext'

function useAlerts() {
  const context = useContext(AlertsContext)

  if (!context) {
    throw new Error('useAlerts deve ser usado dentro de AlertsProvider')
  }

  return context
}

export default useAlerts
