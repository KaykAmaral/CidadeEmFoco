import { useEffect, useRef, useState } from 'react'
import useAuth from './useAuth'
import { getMapOccurrences } from '../services/occurrenceService'
import { startMapPolling } from '../utils/mapPolling'

function useOccurrenceMap(filters, refreshKey) {
  const { token, logout } = useAuth()
  const query = JSON.stringify(filters)
  const [state, setState] = useState(null)
  const polling = useRef(null)

  useEffect(() => {
    const poller = startMapPolling(
      (signal) => getMapOccurrences(token, JSON.parse(query), signal),
      (occurrences) => setState({ query, refreshKey, token, occurrences, error: '' }),
      (error) => {
        if (error.status === 401) logout()
        else setState({ query, refreshKey, token, occurrences: [], error: error.message })
      },
    )
    polling.current = poller
    const refreshWhenVisible = () => {
      if (document.visibilityState === 'visible') poller.refresh()
    }
    window.addEventListener('focus', refreshWhenVisible)
    document.addEventListener('visibilitychange', refreshWhenVisible)
    return () => {
      poller.stop()
      window.removeEventListener('focus', refreshWhenVisible)
      document.removeEventListener('visibilitychange', refreshWhenVisible)
    }
  }, [query, refreshKey, token, logout])

  const current = state?.query === query && state?.refreshKey === refreshKey && state?.token === token
  return {
    occurrences: current ? state.occurrences : [],
    error: current ? state.error : '',
    isLoading: !current,
    retry: () => polling.current?.refresh(),
  }
}

export default useOccurrenceMap
