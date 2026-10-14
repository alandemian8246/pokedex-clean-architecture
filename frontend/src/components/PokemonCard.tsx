import { Link } from 'react-router'
import type { PokemonSummary } from '../api/types'
import { formatId, formatName } from '../lib/format'
import { Sprite } from './Sprite'
import { TypeBadge } from './TypeBadge'

export function PokemonCard({ pokemon }: { pokemon: PokemonSummary }) {
  return (
    <Link
      to={`/pokemon/${pokemon.name}`}
      className="card group flex flex-col gap-3 p-4 transition hover:-translate-y-0.5 hover:shadow-md"
    >
      <div className="flex items-start justify-between">
        <span className="text-xs font-semibold text-slate-400">{formatId(pokemon.id)}</span>
        <div className="flex gap-1">
          {pokemon.types.map((type) => (
            <TypeBadge key={type} type={type} />
          ))}
        </div>
      </div>
      <div className="flex justify-center">
        <Sprite src={pokemon.spriteUrl} alt={formatName(pokemon.name)} size={96} />
      </div>
      <div>
        <h2 className="text-lg font-bold group-hover:text-brand-600">{formatName(pokemon.name)}</h2>
        <p className="text-sm text-slate-500">{pokemon.category ?? 'Unknown category'}</p>
      </div>
      <dl className="grid grid-cols-[auto_1fr] gap-x-3 gap-y-1 text-sm">
        <dt className="text-slate-500">Weight</dt>
        <dd className="font-medium">{pokemon.weightKg.toFixed(1)} kg</dd>
        <dt className="text-slate-500">Abilities</dt>
        <dd className="font-medium">{pokemon.abilities.map(formatName).join(', ') || '—'}</dd>
      </dl>
    </Link>
  )
}
