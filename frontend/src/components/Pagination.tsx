interface PaginationProps {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
  disabled?: boolean
}

/** Zero-based page navigation; renders nothing when everything fits on one page. */
export function Pagination({ page, totalPages, onPageChange, disabled = false }: PaginationProps) {
  if (totalPages <= 1) return null

  return (
    <nav aria-label="Pagination" className="flex items-center justify-center gap-3 py-6">
      <button
        type="button"
        className="btn-secondary"
        onClick={() => onPageChange(page - 1)}
        disabled={disabled || page <= 0}
      >
        ← Previous
      </button>
      <span className="text-sm text-slate-600" aria-current="page">
        Page <strong>{page + 1}</strong> of {totalPages}
      </span>
      <button
        type="button"
        className="btn-secondary"
        onClick={() => onPageChange(page + 1)}
        disabled={disabled || page >= totalPages - 1}
      >
        Next →
      </button>
    </nav>
  )
}
