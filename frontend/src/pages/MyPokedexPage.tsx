import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import { useDeleteLocalPokemon, useLocalPokemonPage, useRefreshLocalPokemon } from '../api/queries'
import type { LocalPokemon } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { CardGridSkeleton, EmptyState, ErrorMessage } from '../components/Feedback'
import { Pagination } from '../components/Pagination'
import { Sprite } from '../components/Sprite'
import { TypeBadge } from '../components/TypeBadge'
import { formatDate, formatId, formatName } from '../lib/format'

const PAGE_SIZE = 12
const SEARCH_DEBOUNCE_MS = 300

/** Local replica (User Stories 03 and 04): search, refresh, edit and delete stored Pokemon. */
export function MyPokedexPage() {
  const [params, setParams] = useSearchParams()
  const page = Math.max(0, Number(params.get('page') ?? 1) - 1) || 0
  const search = params.get('q') ?? ''
  const [searchInput, setSearchInput] = useState(search)

  useEffect(() => {
    const timer = window.setTimeout(() => {
      if (searchInput.trim() !== search) {
        setParams(searchInput.trim() ? { q: searchInput.trim() } : {}, { replace: true })
      }
    }, SEARCH_DEBOUNCE_MS)
    return () => window.clearTimeout(timer)
  }, [searchInput, search, setParams])

  const { data, isPending, isError, error, refetch, isPlaceholderData } = useLocalPokemonPage(page, PAGE_SIZE, search)

  const goTo = (nextPage: number) => {
    setParams({ ...(search ? { q: search } : {}), page: String(nextPage + 1) })
  }

  return (
    <section className="flex flex-col gap-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="text-3xl font-extrabold">My Pokédex</h1>
          <p className="text-slate-500">Pokémon stored locally, enriched with your own names, regions and tags.</p>
        </div>
        <div role="search" className="md:w-72">
          <label htmlFor="local-search" className="sr-only">
            Search by name or localized name
          </label>
          <input
            id="local-search"
            type="search"
            className="input"
            placeholder="Search name or localized name"
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
          />
        </div>
      </div>

      {isPending ? (
        <CardGridSkeleton count={6} />
      ) : isError ? (
        <ErrorMessage error={error} onRetry={() => refetch()} />
      ) : data.content.length === 0 ? (
        <EmptyState title={search ? `Nothing matches “${search}”` : 'Your Pokédex is empty'}>
          <p className="text-sm text-slate-500">Open any Pokémon in the catalog and choose “Add to my Pokédex”.</p>
          <Link to="/" className="btn-primary">
            Browse the catalog
          </Link>
        </EmptyState>
      ) : (
        <>
          <ul className={`grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 ${isPlaceholderData ? 'opacity-60' : ''}`}>
            {data.content.map((pokemon) => (
              <li key={pokemon.id}>
                <LocalPokemonCard pokemon={pokemon} />
              </li>
            ))}
          </ul>
          <Pagination page={page} totalPages={data.totalPages} onPageChange={goTo} disabled={isPlaceholderData} />
        </>
      )}
    </section>
  )
}

function LocalPokemonCard({ pokemon }: { pokemon: LocalPokemon }) {
  const { isAdmin } = useAuth()
  const refresh = useRefreshLocalPokemon()
  const remove = useDeleteLocalPokemon()
  const [confirmingDelete, setConfirmingDelete] = useState(false)
  const actionError = refresh.error ?? remove.error

  return (
    <article className="card flex h-full flex-col gap-3 p-4">
      <div className="flex items-start gap-3">
        <Sprite src={pokemon.spriteUrl} alt={formatName(pokemon.name)} size={80} />
        <div className="min-w-0 flex-1">
          <p className="text-xs font-semibold text-slate-400">{formatId(pokemon.id)}</p>
          <h2 className="truncate text-lg font-bold">
            <Link to={`/pokemon/${pokemon.name}`} className="hover:text-brand-600">
              {formatName(pokemon.name)}
            </Link>
          </h2>
          {pokemon.localizedName && <p className="truncate text-sm text-slate-600">{pokemon.localizedName}</p>}
          <div className="mt-1 flex flex-wrap gap-1">
            {pokemon.types.map((type) => (
              <TypeBadge key={type} type={type} />
            ))}
          </div>
        </div>
      </div>

      <dl className="grid grid-cols-[auto_1fr] gap-x-3 gap-y-1 text-sm">
        <dt className="text-slate-500">Region</dt>
        <dd className="font-medium">{pokemon.region ?? '—'}</dd>
        <dt className="text-slate-500">Updated</dt>
        <dd className="font-medium">{formatDate(pokemon.updatedAt)}</dd>
      </dl>
      {pokemon.tags.length > 0 && (
        <ul className="flex flex-wrap gap-1" aria-label="Tags">
          {pokemon.tags.map((tag) => (
            <li key={tag} className="rounded-md bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
              #{tag}
            </li>
          ))}
        </ul>
      )}
      {pokemon.notes && <p className="line-clamp-2 text-sm text-slate-600">{pokemon.notes}</p>}

      <div className="mt-auto flex flex-wrap gap-2 pt-2">
        <Link to={`/my-pokedex/${pokemon.id}/edit`} className="btn-primary">
          Edit
        </Link>
        <button
          type="button"
          className="btn-secondary"
          onClick={() => refresh.mutate(pokemon.id)}
          disabled={refresh.isPending}
          title="Re-download data from PokeAPI, keeping your fields"
        >
          {refresh.isPending ? 'Refreshing…' : 'Refresh'}
        </button>
        {isAdmin &&
          (confirmingDelete ? (
            <span className="flex items-center gap-2">
              <button
                type="button"
                className="btn-danger"
                onClick={() => remove.mutate(pokemon.id)}
                disabled={remove.isPending}
              >
                {remove.isPending ? 'Deleting…' : 'Confirm delete'}
              </button>
              <button type="button" className="btn-secondary" onClick={() => setConfirmingDelete(false)}>
                Cancel
              </button>
            </span>
          ) : (
            <button type="button" className="btn-secondary" onClick={() => setConfirmingDelete(true)}>
              Delete
            </button>
          ))}
      </div>
      {actionError && (
        <p role="alert" className="text-sm text-brand-700">
          {actionError.message}
        </p>
      )}
    </article>
  )
}
