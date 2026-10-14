import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { ApiError } from '../api/http'
import { useAuth } from '../auth/useAuth'

type Mode = 'login' | 'register'

const COPY: Record<Mode, { title: string; submit: string; switchText: string; switchLink: string; switchTo: string }> = {
  login: {
    title: 'Log in',
    submit: 'Log in',
    switchText: 'New here?',
    switchLink: 'Create an account',
    switchTo: '/register',
  },
  register: {
    title: 'Create an account',
    submit: 'Sign up',
    switchText: 'Already registered?',
    switchLink: 'Log in',
    switchTo: '/login',
  },
}

export function AuthPage({ mode }: { mode: Mode }) {
  const { login, register, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from ?? '/my-pokedex'

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated && !submitting) return <Navigate to={from} replace />

  const copy = COPY[mode]

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setError(null)
    setFieldErrors({})
    setSubmitting(true)
    try {
      await (mode === 'login' ? login(username, password) : register(username, password))
      navigate(from, { replace: true })
    } catch (caught) {
      if (caught instanceof ApiError) {
        setFieldErrors(caught.fieldErrors)
        setError(Object.keys(caught.fieldErrors).length > 0 ? null : caught.message)
      } else {
        setError('Something went wrong, please try again')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="mx-auto flex max-w-sm flex-col gap-6 py-6">
      <h1 className="text-center text-3xl font-extrabold">{copy.title}</h1>
      <form onSubmit={handleSubmit} className="card flex flex-col gap-4 p-6">
        <div className="flex flex-col gap-1">
          <label htmlFor="username" className="text-sm font-semibold">
            Username
          </label>
          <input
            id="username"
            className="input"
            autoComplete="username"
            required
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            aria-invalid={Boolean(fieldErrors.username)}
          />
          {fieldErrors.username && <p className="text-xs text-brand-700">{fieldErrors.username}</p>}
        </div>
        <div className="flex flex-col gap-1">
          <label htmlFor="password" className="text-sm font-semibold">
            Password
          </label>
          <input
            id="password"
            type="password"
            className="input"
            autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            aria-invalid={Boolean(fieldErrors.password)}
          />
          {fieldErrors.password ? (
            <p className="text-xs text-brand-700">{fieldErrors.password}</p>
          ) : (
            mode === 'register' && <p className="text-xs text-slate-500">At least 8 characters, with a letter and a digit.</p>
          )}
        </div>
        {error && (
          <p role="alert" className="rounded-lg bg-brand-50 p-3 text-sm text-brand-700">
            {error}
          </p>
        )}
        <button type="submit" className="btn-primary" disabled={submitting}>
          {submitting ? 'Please wait…' : copy.submit}
        </button>
      </form>
      <p className="text-center text-sm text-slate-600">
        {copy.switchText}{' '}
        <Link to={copy.switchTo} state={location.state} className="font-semibold text-brand-600 hover:underline">
          {copy.switchLink}
        </Link>
      </p>
      {mode === 'login' && (
        <p className="text-center text-xs text-slate-500">
          Demo accounts: <code>demo / Demo1234</code> (user) · <code>admin / Admin1234</code> (admin)
        </p>
      )}
    </section>
  )
}
