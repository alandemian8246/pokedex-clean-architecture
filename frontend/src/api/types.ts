export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface PokemonSummary {
  id: number
  name: string
  category: string | null
  weight: number
  weightKg: number
  spriteUrl: string | null
  types: string[]
  abilities: string[]
}

export interface Stat {
  name: string
  value: number
}

export interface Evolution {
  id: number
  name: string
  spriteUrl: string
  trigger: string | null
  evolvesTo: Evolution[]
}

export interface PokemonDetail {
  id: number
  name: string
  category: string | null
  heightM: number
  weightKg: number
  spriteUrl: string | null
  artworkUrl: string | null
  types: string[]
  abilities: string[]
  stats: Stat[]
  description: string | null
  evolutionChain: Evolution | null
}

export interface LocalPokemon {
  id: number
  name: string
  category: string | null
  heightM: number
  weightKg: number
  spriteUrl: string | null
  artworkUrl: string | null
  description: string | null
  types: string[]
  abilities: string[]
  stats: Stat[]
  localizedName: string | null
  region: string | null
  tags: string[]
  notes: string | null
  syncedAt: string
  updatedAt: string
  version: number
}

export interface UpdateLocalPokemonPayload {
  localizedName: string | null
  region: string | null
  tags: string[]
  notes: string | null
  version: number
}

export type Role = 'USER' | 'ADMIN'

export interface User {
  id: number
  username: string
  role: Role
  createdAt: string
}

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresAt: string
  user: User
}

/** RFC 9457 error body returned by every failing API call. */
export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  errors?: Record<string, string>
}
