import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { authApi } from '../api/endpoints'
import { setAuthToken, UNAUTHORIZED_EVENT } from '../api/http'
import { queryKeys } from '../api/queries'
import type { User } from '../api/types'
import { AuthContext, type AuthContextValue } from './AuthContext'

interface Session {
  token: string
  expiresAt: string
  user: User
}

const STORAGE_KEY = 'pokedex.session'

function loadSession(): Session | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const session = JSON.parse(raw) as Session
    if (new Date(session.expiresAt).getTime() <= Date.now()) {
      localStorage.removeItem(STORAGE_KEY)
      return null
    }
    return session
  } catch {
    return null
  }
}

function persist(session: Session | null) {
  try {
    if (session) localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
    else localStorage.removeItem(STORAGE_KEY)
  } catch {
    // storage can be unavailable (private mode); the session then lasts for this tab only
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [session, setSession] = useState<Session | null>(() => {
    const restored = loadSession()
    setAuthToken(restored?.token ?? null)
    return restored
  })

  const applySession = useCallback((next: Session | null) => {
    setAuthToken(next?.token ?? null)
    persist(next)
    setSession(next)
  }, [])

  const logout = useCallback(() => {
    applySession(null)
    queryClient.removeQueries({ queryKey: queryKeys.localAll })
  }, [applySession, queryClient])

  const login = useCallback(
    async (username: string, password: string) => {
      const response = await authApi.login(username, password)
      applySession({ token: response.accessToken, expiresAt: response.expiresAt, user: response.user })
    },
    [applySession],
  )

  const register = useCallback(
    async (username: string, password: string) => {
      await authApi.register(username, password)
      await login(username, password)
    },
    [login],
  )

  useEffect(() => {
    window.addEventListener(UNAUTHORIZED_EVENT, logout)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, logout)
  }, [logout])

  useEffect(() => {
    if (!session) return
    const remaining = new Date(session.expiresAt).getTime() - Date.now()
    const timer = window.setTimeout(logout, Math.max(remaining, 0))
    return () => window.clearTimeout(timer)
  }, [session, logout])

  const value = useMemo<AuthContextValue>(
    () => ({
      user: session?.user ?? null,
      isAuthenticated: session !== null,
      isAdmin: session?.user.role === 'ADMIN',
      login,
      register,
      logout,
    }),
    [session, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
