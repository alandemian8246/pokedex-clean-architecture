import { Link } from 'react-router'
import type { Evolution } from '../api/types'
import { formatName } from '../lib/format'
import { Sprite } from './Sprite'

interface EvolutionTreeProps {
  chain: Evolution
  currentId: number
}

/** Renders linear chains as a row and branching chains (e.g. Eevee) as a wrapped list of branches. */
export function EvolutionTree({ chain, currentId }: EvolutionTreeProps) {
  if (chain.evolvesTo.length === 0) {
    return <p className="text-sm text-slate-500">This Pokémon does not evolve.</p>
  }
  return (
    <ol className="flex flex-wrap items-center justify-center gap-2">
      <EvolutionStage stage={chain} currentId={currentId} />
    </ol>
  )
}

function EvolutionStage({ stage, currentId }: { stage: Evolution; currentId: number }) {
  const isCurrent = stage.id === currentId
  return (
    <>
      <li>
        <Link
          to={`/pokemon/${stage.name}`}
          aria-current={isCurrent ? 'page' : undefined}
          className={`flex w-28 flex-col items-center rounded-xl p-2 text-center transition hover:bg-slate-100 ${isCurrent ? 'bg-brand-50 ring-2 ring-brand-500' : ''}`}
        >
          <Sprite src={stage.spriteUrl} alt={formatName(stage.name)} size={72} />
          <span className="text-sm font-semibold">{formatName(stage.name)}</span>
        </Link>
      </li>
      {stage.evolvesTo.length > 0 && (
        <li className="flex flex-col gap-2">
          {stage.evolvesTo.map((next) => (
            <ol key={next.id} className="flex flex-wrap items-center gap-2">
              <li className="flex w-24 flex-col items-center text-center text-xs text-slate-500">
                <span aria-hidden="true" className="text-lg text-slate-400">→</span>
                <span>{next.trigger ?? 'Evolves'}</span>
              </li>
              <EvolutionStage stage={next} currentId={currentId} />
            </ol>
          ))}
        </li>
      )}
    </>
  )
}
