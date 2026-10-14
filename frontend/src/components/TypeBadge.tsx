import { formatName } from '../lib/format'

const TYPE_COLORS: Record<string, string> = {
  normal: 'bg-stone-400',
  fire: 'bg-orange-500',
  water: 'bg-blue-500',
  electric: 'bg-yellow-400 text-slate-900',
  grass: 'bg-green-500',
  ice: 'bg-cyan-400 text-slate-900',
  fighting: 'bg-red-700',
  poison: 'bg-purple-500',
  ground: 'bg-amber-600',
  flying: 'bg-indigo-400',
  psychic: 'bg-pink-500',
  bug: 'bg-lime-600',
  rock: 'bg-yellow-700',
  ghost: 'bg-violet-700',
  dragon: 'bg-indigo-700',
  dark: 'bg-neutral-700',
  steel: 'bg-slate-500',
  fairy: 'bg-pink-300 text-slate-900',
}

export function TypeBadge({ type }: { type: string }) {
  const color = TYPE_COLORS[type] ?? 'bg-slate-400'
  return (
    <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold text-white ${color}`}>
      {formatName(type)}
    </span>
  )
}
