import { apiFetch } from './http'
import type {
  LocalPokemon,
  Page,
  PokemonDetail,
  PokemonSummary,
  TokenResponse,
  UpdateLocalPokemonPayload,
  User,
} from './types'

const query = (params: Record<string, string | number | undefined>) => {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') search.set(key, String(value))
  })
  return search.toString()
}

export const pokemonApi = {
  list: (page: number, size: number, signal?: AbortSignal) =>
    apiFetch<Page<PokemonSummary>>(`/api/pokemon?${query({ page, size })}`, { signal }),
  get: (idOrName: string, signal?: AbortSignal) =>
    apiFetch<PokemonDetail>(`/api/pokemon/${encodeURIComponent(idOrName)}`, { signal }),
}

export const localPokemonApi = {
  list: (page: number, size: number, search?: string, signal?: AbortSignal) =>
    apiFetch<Page<LocalPokemon>>(`/api/local-pokemon?${query({ page, size, search })}`, { signal }),
  get: (id: number, signal?: AbortSignal) => apiFetch<LocalPokemon>(`/api/local-pokemon/${id}`, { signal }),
  sync: (idOrName: string) => apiFetch<LocalPokemon>('/api/local-pokemon', { method: 'POST', body: { idOrName } }),
  refresh: (id: number) => apiFetch<LocalPokemon>(`/api/local-pokemon/${id}/refresh`, { method: 'POST' }),
  update: (id: number, payload: UpdateLocalPokemonPayload) =>
    apiFetch<LocalPokemon>(`/api/local-pokemon/${id}`, { method: 'PUT', body: payload }),
  remove: (id: number) => apiFetch<void>(`/api/local-pokemon/${id}`, { method: 'DELETE' }),
}

export const authApi = {
  login: (username: string, password: string) =>
    apiFetch<TokenResponse>('/api/auth/login', { method: 'POST', body: { username, password } }),
  register: (username: string, password: string) =>
    apiFetch<User>('/api/auth/register', { method: 'POST', body: { username, password } }),
}
