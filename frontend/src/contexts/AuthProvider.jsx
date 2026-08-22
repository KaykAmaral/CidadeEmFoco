import { useCallback, useEffect, useMemo, useState } from 'react'
import { loginRequest, registerRequest } from '../services/authService'
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

  const login = useCallback(async (credentials) => {
    const response = await loginRequest(credentials)
    const newSession = {
      token: response.token,
      tokenType: response.tokenType,
      expiresAt: Date.now() + response.expiresInSeconds * 1000,
      user: response.user,
    }

    localStorage.setItem(STORAGE_KEY, JSON.stringify(newSession))
    setSession(newSession)
    return response.user
  }, [])

  const register = useCallback((citizen) => registerRequest(citizen), [])

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
