import { useState, type FormEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import { usePokemonPage } from '../api/queries'
import { CardGridSkeleton, ErrorMessage } from '../components/Feedback'
import { Pagination } from '../components/Pagination'
import { PokemonCard } from '../components/PokemonCard'

const PAGE_SIZES = [12, 24, 48]

function readNumber(params: URLSearchParams, key: string, fallback: number) {
  const value = Number(params.get(key))
  return Number.isInteger(value) && value > 0 ? value : fallback
}

/** User Story 01: paginated catalog. Page state lives in the URL so it survives reloads and back navigation. */
export function CatalogPage() {
  const [params, setParams] = useSearchParams()
  const navigate = useNavigate()
  const page = readNumber(params, 'page', 1) - 1
  const size = PAGE_SIZES.includes(readNumber(params, 'size', 24)) ? readNumber(params, 'size', 24) : 24
  const [lookup, setLookup] = useState('')

  const { data, isPending, isError, error, refetch, isPlaceholderData } = usePokemonPage(page, size)

  const goTo = (nextPage: number, nextSize = size) => {
    setParams({ page: String(nextPage + 1), size: String(nextSize) })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleLookup = (event: FormEvent) => {
    event.preventDefault()
    const value = lookup.trim().toLowerCase()
    if (value) navigate(`/pokemon/${encodeURIComponent(value)}`)
  }

  return (
    <section className="flex flex-col gap-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="text-3xl font-extrabold">Pokémon catalog</h1>
          <p className="text-slate-500">
            {data ? `${data.totalElements.toLocaleString()} Pokémon from PokeAPI` : 'Browse every Pokémon from PokeAPI'}
          </p>
        </div>
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
          <form onSubmit={handleLookup} className="flex gap-2" role="search">
            <label htmlFor="lookup" className="sr-only">
              Go to Pokémon by name or number
            </label>
            <input
              id="lookup"
              className="input sm:w-56"
              placeholder="Name or number, e.g. pikachu"
              value={lookup}
              onChange={(event) => setLookup(event.target.value)}
            />
            <button type="submit" className="btn-primary">
              Go
            </button>
          </form>
          <label className="flex items-center gap-2 text-sm text-slate-600">
            Per page
            <select
              className="input w-auto"
              value={size}
              onChange={(event) => goTo(0, Number(event.target.value))}
            >
              {PAGE_SIZES.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </label>
        </div>
      </div>

      {isPending ? (
        <CardGridSkeleton count={size > 12 ? 12 : size} />
      ) : isError ? (
        <ErrorMessage error={error} onRetry={() => refetch()} />
      ) : (
        <>
          <div
            className={`grid grid-cols-1 gap-4 transition-opacity sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 ${isPlaceholderData ? 'opacity-60' : ''}`}
            aria-busy={isPlaceholderData}
          >
            {data.content.map((pokemon) => (
              <PokemonCard key={pokemon.id} pokemon={pokemon} />
            ))}
          </div>
          <Pagination page={page} totalPages={data.totalPages} onPageChange={goTo} disabled={isPlaceholderData} />
        </>
      )}
    </section>
  )
}
