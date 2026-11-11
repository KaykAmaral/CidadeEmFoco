import { useCallback, useEffect, useMemo, useState } from 'react'
import { loginRequest, registerAndLoginRequest } from '../services/authService'
import { createSession } from '../utils/authSession'
import AuthContext from './authContext'

const STORAGE_KEY = 'cidade-em-foco:auth'

function readStoredSession() {
  try {
    const storedSession = JSON.parse(localStorage.getItem(STORAGE_KEY))

    if (
      !storedSession?.token ||
      !storedSession?.user ||
      storedSession.expiresAt <= Date.now()
    ) {
      localStorage.removeItem(STORAGE_KEY)
      return null
    }

    return storedSession
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

function AuthProvider({ children }) {
  const [session, setSession] = useState(readStoredSession)

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    setSession(null)
  }, [])

  useEffect(() => {
    if (!session?.expiresAt) {
      return undefined
    }

    const remainingTime = session.expiresAt - Date.now()

    const timer = window.setTimeout(logout, Math.max(remainingTime, 0))
    return () => window.clearTimeout(timer)
  }, [logout, session])

  const saveSession = useCallback((response) => {
    const newSession = createSession(response)

    localStorage.setItem(STORAGE_KEY, JSON.stringify(newSession))
    setSession(newSession)
    return response.user
  }, [])

  const login = useCallback(async (credentials) => {
    const response = await loginRequest(credentials)
    return saveSession(response)
  }, [saveSession])

  const register = useCallback(async (citizen) => {
    const response = await registerAndLoginRequest(citizen)
    return saveSession(response)
  }, [saveSession])

  const value = useMemo(
    () => ({
      isAuthenticated: Boolean(session),
      token: session?.token ?? null,
      user: session?.user ?? null,
      login,
      logout,
      register,
    }),
    [login, logout, register, session],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export default AuthProvider
