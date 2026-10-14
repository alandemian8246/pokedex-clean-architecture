import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { mockFetch, renderWithProviders } from '../test/render'
import { AuthPage } from './AuthPage'

const tokenResponse = {
  accessToken: 'jwt',
  tokenType: 'Bearer',
  expiresAt: new Date(Date.now() + 3_600_000).toISOString(),
  user: { id: 1, username: 'demo', role: 'USER', createdAt: '2026-01-01T00:00:00Z' },
}

async function fillAndSubmit(username: string, password: string, button: RegExp) {
  await userEvent.type(screen.getByLabelText(/username/i), username)
  await userEvent.type(screen.getByLabelText(/password/i), password)
  await userEvent.click(screen.getByRole('button', { name: button }))
}

describe('AuthPage', () => {
  it('logs in, stores the session and redirects to My Pokédex', async () => {
    const fetchMock = mockFetch({ status: 200, body: tokenResponse })
    renderWithProviders(<AuthPage mode="login" />, { route: '/login', path: '/login' })

    await fillAndSubmit('demo', 'Demo1234', /log in/i)

    expect(await screen.findByText('my pokedex page')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledWith('/api/auth/login', expect.objectContaining({ method: 'POST' }))
    expect(localStorage.getItem('pokedex.session')).toContain('jwt')
  })

  it('shows the server message for invalid credentials', async () => {
    mockFetch({ status: 401, body: { title: 'Authentication failed', detail: 'Invalid username or password' } })
    renderWithProviders(<AuthPage mode="login" />, { route: '/login', path: '/login' })

    await fillAndSubmit('demo', 'wrong', /log in/i)

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid username or password')
  })

  it('shows field errors returned by registration', async () => {
    mockFetch({
      status: 400,
      body: { title: 'Validation failed', errors: { password: 'must contain at least one letter and one digit' } },
    })
    renderWithProviders(<AuthPage mode="register" />, { route: '/register', path: '/register' })

    await fillAndSubmit('newbie', 'onlyletters', /sign up/i)

    expect(await screen.findByText('must contain at least one letter and one digit')).toBeInTheDocument()
  })
})
