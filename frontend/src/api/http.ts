import type { ProblemDetail } from './types'

/** Error thrown for any non-2xx response, carrying the server's ProblemDetail. */
export class ApiError extends Error {
  readonly status: number
  readonly title: string
  readonly fieldErrors: Record<string, string>

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `Request failed with status ${status}`)
    this.name = 'ApiError'
    this.status = status
    this.title = problem.title ?? 'Error'
    this.fieldErrors = problem.errors ?? {}
  }
}

export const UNAUTHORIZED_EVENT = 'pokedex:unauthorized'

const BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? ''

let authToken: string | null = null

export function setAuthToken(token: string | null): void {
  authToken = token
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  signal?: AbortSignal
}

export async function apiFetch<T>(path: string, { method = 'GET', body, signal }: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (authToken) headers.Authorization = `Bearer ${authToken}`

  let response: Response
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw new ApiError(0, {
      title: 'Network error',
      detail: 'Could not reach the server. Check your connection and try again.',
    })
  }

  if (response.status === 401 && authToken) {
    // the stored session is no longer valid: let the auth layer sign the user out
    window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
  }
  if (!response.ok) {
    throw new ApiError(response.status, await readProblem(response))
  }
  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

async function readProblem(response: Response): Promise<ProblemDetail> {
  try {
    return (await response.json()) as ProblemDetail
  } catch {
    return { title: response.statusText || 'Error' }
  }
}
