import { useContext } from 'react'
import AlertContext from '../contexts/alertContext'

function useAlerts() {
  const context = useContext(AlertContext)
  if (!context) throw new Error('useAlerts deve ser usado dentro de AlertProvider.')
  return context
}

export default useAlerts
