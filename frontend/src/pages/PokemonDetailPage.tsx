import { Link, useParams } from 'react-router'
import { ApiError } from '../api/http'
import { useLocalPokemonPage, usePokemon, useSyncPokemon } from '../api/queries'
import type { PokemonDetail } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { EvolutionTree } from '../components/EvolutionTree'
import { EmptyState, ErrorMessage, Spinner } from '../components/Feedback'
import { Sprite } from '../components/Sprite'
import { StatBars } from '../components/StatBars'
import { TypeBadge } from '../components/TypeBadge'
import { formatId, formatName } from '../lib/format'

/** User Story 02: image, stats, description and evolution chain of one Pokemon. */
export function PokemonDetailPage() {
  const { idOrName = '' } = useParams()
  const { data, isPending, isError, error, refetch } = usePokemon(idOrName)

  if (isPending) return <Spinner label="Loading Pokémon" />
  if (isError) {
    if (error instanceof ApiError && (error.status === 404 || error.status === 400)) {
      return (
        <EmptyState title={`No Pokémon called “${idOrName}”`}>
          <Link to="/" className="btn-primary">
            Back to the catalog
          </Link>
        </EmptyState>
      )
    }
    return <ErrorMessage error={error} onRetry={() => refetch()} />
  }

  return (
    <article className="flex flex-col gap-6">
      <Link to="/" className="text-sm font-semibold text-brand-600 hover:underline">
        ← Back to catalog
      </Link>

      <div className="grid gap-6 lg:grid-cols-[minmax(0,2fr)_minmax(0,3fr)]">
        <section className="card flex flex-col items-center gap-4 p-6">
          <Sprite src={data.artworkUrl} alt={formatName(data.name)} size={256} pixelated={false} eager />
          <div className="text-center">
            <p className="text-sm font-semibold text-slate-400">{formatId(data.id)}</p>
            <h1 className="text-3xl font-extrabold">{formatName(data.name)}</h1>
            <p className="text-slate-500">{data.category}</p>
          </div>
          <div className="flex gap-2">
            {data.types.map((type) => (
              <TypeBadge key={type} type={type} />
            ))}
          </div>
          <SyncPanel pokemon={data} />
        </section>

        <div className="flex flex-col gap-6">
          <section className="card p-6">
            <h2 className="mb-2 text-lg font-bold">Description</h2>
            <p className="text-slate-700">{data.description ?? 'No description available.'}</p>
            <dl className="mt-4 grid grid-cols-3 gap-4 text-center">
              <div className="rounded-xl bg-slate-50 p-3">
                <dt className="text-xs text-slate-500">Height</dt>
                <dd className="font-bold">{data.heightM.toFixed(1)} m</dd>
              </div>
              <div className="rounded-xl bg-slate-50 p-3">
                <dt className="text-xs text-slate-500">Weight</dt>
                <dd className="font-bold">{data.weightKg.toFixed(1)} kg</dd>
              </div>
              <div className="rounded-xl bg-slate-50 p-3">
                <dt className="text-xs text-slate-500">Abilities</dt>
                <dd className="font-bold">{data.abilities.map(formatName).join(', ')}</dd>
              </div>
            </dl>
          </section>

          <section className="card p-6">
            <h2 className="mb-4 text-lg font-bold">Base stats</h2>
            <StatBars stats={data.stats} />
          </section>
        </div>
      </div>

      <section className="card p-6">
        <h2 className="mb-4 text-lg font-bold">Evolution chain</h2>
        {data.evolutionChain ? (
          <EvolutionTree chain={data.evolutionChain} currentId={data.id} />
        ) : (
          <p className="text-sm text-slate-500">No evolution data available.</p>
        )}
      </section>
    </article>
  )
}

/** User Story 03 entry point: copy this Pokemon into the user's local Pokedex. */
function SyncPanel({ pokemon }: { pokemon: PokemonDetail }) {
  const { isAuthenticated } = useAuth()
  // a 200 search (rather than probing GET /{id} for a 404) keeps the browser console free of errors
  const stored = useLocalPokemonPage(0, 20, pokemon.name, isAuthenticated)
  const sync = useSyncPokemon()

  if (!isAuthenticated) {
    return (
      <p className="text-center text-sm text-slate-500">
        <Link to="/login" className="font-semibold text-brand-600 hover:underline">
          Log in
        </Link>{' '}
        to add this Pokémon to your Pokédex.
      </p>
    )
  }

  const isStored = sync.isSuccess || stored.data?.content.some((entry) => entry.id === pokemon.id)
  if (isStored) {
    return (
      <Link to={`/my-pokedex/${pokemon.id}/edit`} className="btn-secondary">
        ✓ In your Pokédex — edit details
      </Link>
    )
  }

  return (
    <div className="flex flex-col items-center gap-2" aria-live="polite">
      <button
        type="button"
        className="btn-primary"
        onClick={() => sync.mutate(String(pokemon.id))}
        disabled={sync.isPending || stored.isPending}
      >
        {sync.isPending ? 'Saving…' : '+ Add to my Pokédex'}
      </button>
      {sync.isError && <p className="text-sm text-brand-700">{sync.error.message}</p>}
    </div>
  )
}
