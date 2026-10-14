import type { ReactElement } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { vi } from 'vitest'
import { AuthProvider } from '../auth/AuthProvider'

/** Renders a page inside the same providers the app uses, at the given route. */
export function renderWithProviders(ui: ReactElement, { route = '/', path = '*' } = {}) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <MemoryRouter initialEntries={[route]}>
          <Routes>
            <Route path={path} element={ui} />
            <Route path="/my-pokedex" element={<p>my pokedex page</p>} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  )
}

/** Stubs global fetch with one JSON response per call, in order. */
export function mockFetch(...responses: Array<{ status: number; body?: unknown }>) {
  const fetchMock = vi.fn()
  responses.forEach(({ status, body }) => {
    fetchMock.mockResolvedValueOnce(
      new Response(body === undefined ? null : JSON.stringify(body), {
        status,
        headers: { 'Content-Type': 'application/json' },
      }),
    )
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}
