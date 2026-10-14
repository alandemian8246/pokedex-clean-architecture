import type { ReactNode } from 'react'
import { ApiError } from '../api/http'

export function Spinner({ label = 'Loading' }: { label?: string }) {
  return (
    <div role="status" className="flex items-center justify-center gap-3 py-16 text-slate-500">
      <span className="h-6 w-6 animate-spin rounded-full border-4 border-brand-100 border-t-brand-600" />
      <span>{label}…</span>
    </div>
  )
}

export function CardGridSkeleton({ count = 8 }: { count?: number }) {
  return (
    <div aria-hidden="true" className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {Array.from({ length: count }, (_, index) => (
        <div key={index} className="card h-56 animate-pulse bg-slate-100" />
      ))}
    </div>
  )
}

export function ErrorMessage({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const message = error instanceof Error ? error.message : 'Something went wrong'
  const title = error instanceof ApiError ? error.title : 'Error'
  return (
    <div role="alert" className="card flex flex-col items-center gap-3 border-brand-100 bg-brand-50 p-8 text-center">
      <p className="font-semibold text-brand-700">{title}</p>
      <p className="text-sm text-slate-700">{message}</p>
      {onRetry && (
        <button type="button" className="btn-secondary" onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  )
}

export function EmptyState({ title, children }: { title: string; children?: ReactNode }) {
  return (
    <div className="card flex flex-col items-center gap-3 p-10 text-center">
      <p className="text-lg font-semibold">{title}</p>
      {children}
    </div>
  )
}
