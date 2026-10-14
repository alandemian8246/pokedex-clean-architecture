import { Link } from 'react-router'
import type { Evolution } from '../api/types'
import { formatName } from '../lib/format'
import { Sprite } from './Sprite'

interface EvolutionTreeProps {
  chain: Evolution
  currentId: number
}

/** Linear chains render as a row; branching chains (e.g. Eevee) show the branches in a responsive grid. */
export function EvolutionTree({ chain, currentId }: EvolutionTreeProps) {
  if (chain.evolvesTo.length === 0) {
    return <p className="text-sm text-slate-500">This Pokémon does not evolve.</p>
  }
  return <EvolutionStage stage={chain} currentId={currentId} />
}

function EvolutionStage({ stage, currentId }: { stage: Evolution; currentId: number }) {
  const [onlyChild] = stage.evolvesTo
  const isBranching = stage.evolvesTo.length > 1

  return (
    <div className={`flex items-center justify-center gap-2 ${isBranching ? 'flex-col' : 'flex-wrap'}`}>
      <StageLink stage={stage} currentId={currentId} />
      {isBranching ? (
        <ul className="grid w-full grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
          {stage.evolvesTo.map((next) => (
            <li key={next.id} className="flex flex-col items-center gap-1 rounded-xl border border-slate-100 p-2">
              <span className="text-center text-xs text-slate-500">{next.trigger ?? 'Evolves'}</span>
              <EvolutionStage stage={next} currentId={currentId} />
            </li>
          ))}
        </ul>
      ) : (
        onlyChild && (
          <>
            <span className="flex w-24 flex-col items-center text-center text-xs text-slate-500">
              <span aria-hidden="true" className="text-lg text-slate-400">
                →
              </span>
              {onlyChild.trigger ?? 'Evolves'}
            </span>
            <EvolutionStage stage={onlyChild} currentId={currentId} />
          </>
        )
      )}
    </div>
  )
}

function StageLink({ stage, currentId }: { stage: Evolution; currentId: number }) {
  const isCurrent = stage.id === currentId
  return (
    <Link
      to={`/pokemon/${stage.name}`}
      aria-current={isCurrent ? 'page' : undefined}
      className={`flex w-28 flex-col items-center rounded-xl p-2 text-center transition hover:bg-slate-100 ${isCurrent ? 'bg-brand-50 ring-2 ring-brand-500' : ''}`}
    >
      <Sprite src={stage.spriteUrl} alt={formatName(stage.name)} size={72} />
      <span className="text-sm font-semibold">{formatName(stage.name)}</span>
    </Link>
  )
}
