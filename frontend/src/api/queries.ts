import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { localPokemonApi, pokemonApi } from './endpoints'
import type { LocalPokemon, UpdateLocalPokemonPayload } from './types'

export const queryKeys = {
  catalog: (page: number, size: number) => ['pokemon', 'list', page, size] as const,
  pokemon: (idOrName: string) => ['pokemon', 'detail', idOrName] as const,
  localAll: ['local-pokemon'] as const,
  localList: (page: number, size: number, search: string) => ['local-pokemon', 'list', page, size, search] as const,
  local: (id: number) => ['local-pokemon', 'detail', id] as const,
}

// PokeAPI data is effectively static, so catalog results stay fresh for a long time
const CATALOG_STALE_TIME = 10 * 60 * 1000

export function usePokemonPage(page: number, size: number) {
  return useQuery({
    queryKey: queryKeys.catalog(page, size),
    queryFn: ({ signal }) => pokemonApi.list(page, size, signal),
    placeholderData: keepPreviousData,
    staleTime: CATALOG_STALE_TIME,
  })
}

export function usePokemon(idOrName: string) {
  return useQuery({
    queryKey: queryKeys.pokemon(idOrName),
    queryFn: ({ signal }) => pokemonApi.get(idOrName, signal),
    staleTime: CATALOG_STALE_TIME,
  })
}

export function useLocalPokemonPage(page: number, size: number, search: string, enabled = true) {
  return useQuery({
    queryKey: queryKeys.localList(page, size, search),
    queryFn: ({ signal }) => localPokemonApi.list(page, size, search, signal),
    placeholderData: keepPreviousData,
    enabled,
  })
}

export function useLocalPokemon(id: number, enabled = true) {
  return useQuery({
    queryKey: queryKeys.local(id),
    queryFn: ({ signal }) => localPokemonApi.get(id, signal),
    enabled,
  })
}

function useStoreLocalPokemon() {
  const queryClient = useQueryClient()
  return (pokemon: LocalPokemon) => {
    queryClient.setQueryData(queryKeys.local(pokemon.id), pokemon)
    return queryClient.invalidateQueries({ queryKey: ['local-pokemon', 'list'] })
  }
}

export function useSyncPokemon() {
  const store = useStoreLocalPokemon()
  return useMutation({ mutationFn: localPokemonApi.sync, onSuccess: store })
}

export function useRefreshLocalPokemon() {
  const store = useStoreLocalPokemon()
  return useMutation({ mutationFn: localPokemonApi.refresh, onSuccess: store })
}

export function useUpdateLocalPokemon(id: number) {
  const store = useStoreLocalPokemon()
  return useMutation({
    mutationFn: (payload: UpdateLocalPokemonPayload) => localPokemonApi.update(id, payload),
    onSuccess: store,
  })
}

export function useDeleteLocalPokemon() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: localPokemonApi.remove,
    onSuccess: (_, id) => {
      queryClient.removeQueries({ queryKey: queryKeys.local(id) })
      return queryClient.invalidateQueries({ queryKey: ['local-pokemon', 'list'] })
    },
  })
}
