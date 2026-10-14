import { afterEach, describe, expect, it, vi } from 'vitest'
import { mockFetch } from '../test/render'
import { ApiError, apiFetch, setAuthToken, UNAUTHORIZED_EVENT } from './http'

describe('apiFetch', () => {
  afterEach(() => setAuthToken(null))

  it('returns parsed JSON and sends the bearer token when present', async () => {
    const fetchMock = mockFetch({ status: 200, body: { ok: true } })
    setAuthToken('abc')

    await expect(apiFetch('/api/thing')).resolves.toEqual({ ok: true })

    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect((init.headers as Record<string, string>).Authorization).toBe('Bearer abc')
  })

  it('serializes request bodies as JSON', async () => {
    const fetchMock = mockFetch({ status: 201, body: {} })

    await apiFetch('/api/thing', { method: 'POST', body: { name: 'pikachu' } })

    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(init.body).toBe('{"name":"pikachu"}')
    expect((init.headers as Record<string, string>)['Content-Type']).toBe('application/json')
  })

  it('throws ApiError with the ProblemDetail fields', async () => {
    mockFetch({
      status: 400,
      body: { title: 'Validation failed', detail: 'One or more fields are invalid', errors: { tags: 'bad' } },
    })

    const error = await apiFetch('/api/thing').catch((caught: unknown) => caught)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 400, title: 'Validation failed', fieldErrors: { tags: 'bad' } })
  })

  it('resolves undefined for 204 responses', async () => {
    mockFetch({ status: 204 })

    await expect(apiFetch('/api/thing', { method: 'DELETE' })).resolves.toBeUndefined()
  })

  it('announces an expired session when an authenticated call gets 401', async () => {
    mockFetch({ status: 401, body: { title: 'Unauthorized' } })
    setAuthToken('expired')
    const listener = vi.fn()
    window.addEventListener(UNAUTHORIZED_EVENT, listener)

    await expect(apiFetch('/api/local-pokemon')).rejects.toBeInstanceOf(ApiError)

    expect(listener).toHaveBeenCalledOnce()
    window.removeEventListener(UNAUTHORIZED_EVENT, listener)
  })

  it('maps network failures to a friendly ApiError', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    await expect(apiFetch('/api/thing')).rejects.toMatchObject({ status: 0, title: 'Network error' })
  })
})
