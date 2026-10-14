import type { Stat } from '../api/types'
import { statLabel } from '../lib/format'

const MAX_BASE_STAT = 255

export function StatBars({ stats }: { stats: Stat[] }) {
  const total = stats.reduce((sum, stat) => sum + stat.value, 0)
  return (
    <dl className="flex flex-col gap-2">
      {stats.map((stat) => {
        const percent = Math.min(100, Math.round((stat.value / MAX_BASE_STAT) * 100))
        return (
          <div key={stat.name} className="grid grid-cols-[5rem_2.5rem_1fr] items-center gap-2 text-sm">
            <dt className="text-slate-500">{statLabel(stat.name)}</dt>
            <dd className="text-right font-semibold tabular-nums">{stat.value}</dd>
            <div
              className="h-2.5 overflow-hidden rounded-full bg-slate-100"
              role="meter"
              aria-label={statLabel(stat.name)}
              aria-valuemin={0}
              aria-valuemax={MAX_BASE_STAT}
              aria-valuenow={stat.value}
            >
              <div
                className={`h-full rounded-full ${stat.value >= 100 ? 'bg-emerald-500' : stat.value >= 60 ? 'bg-amber-400' : 'bg-brand-500'}`}
                style={{ width: `${percent}%` }}
              />
            </div>
          </div>
        )
      })}
      <div className="grid grid-cols-[5rem_2.5rem_1fr] gap-2 border-t border-slate-100 pt-2 text-sm">
        <dt className="text-slate-500">Total</dt>
        <dd className="text-right font-bold tabular-nums">{total}</dd>
      </div>
    </dl>
  )
}
