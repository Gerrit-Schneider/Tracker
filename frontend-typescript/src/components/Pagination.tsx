interface PaginationProps {
  page: number
  totalPages: number
  totalElements: number
  loading: boolean
  onPageChange: (page: number) => void
}

export function Pagination({
  page,
  totalPages,
  totalElements,
  loading,
  onPageChange,
}: PaginationProps) {
  const displayedTotalPages = Math.max(totalPages, 1)

  return (
    <nav
      className="pagination"
      aria-label="Seitennavigation"
    >
      <span>{totalElements} Treffer</span>

      <div className="pagination-controls">
        <button
          type="button"
          disabled={loading || page === 0}
          onClick={() => onPageChange(page - 1)}
        >
          Zurück
        </button>

        <strong>
          Seite {page + 1} von {displayedTotalPages}
        </strong>

        <button
          type="button"
          disabled={
            loading ||
            totalPages === 0 ||
            page >= totalPages - 1
          }
          onClick={() => onPageChange(page + 1)}
        >
          Weiter
        </button>
      </div>
    </nav>
  )
}