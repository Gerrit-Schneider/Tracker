import type { FormEvent } from 'react'
import type { TrainingType } from '../types/training'

export interface TrainingSessionFilterValues {
  type: TrainingType | ''
  from: string
  to: string
  query: string
}

interface TrainingSessionFiltersProps {
  filters: TrainingSessionFilterValues
  loading: boolean
  onChange: (filters: TrainingSessionFilterValues) => void
  onSearch: () => void
  onReset: () => void
}

export function TrainingSessionFilters({
  filters,
  loading,
  onChange,
  onSearch,
  onReset,
}: TrainingSessionFiltersProps) {
  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    onSearch()
  }

  return (
    <section className="filter-panel">
      <div>
        <p className="eyebrow">Suche</p>
        <h2>Trainingseinheiten filtern</h2>
      </div>

      <form className="filter-form" onSubmit={handleSubmit}>
        <label>
          Suchbegriff
          <input
            type="search"
            value={filters.query}
            placeholder="Notizen durchsuchen"
            onChange={(event) =>
              onChange({
                ...filters,
                query: event.target.value,
              })
            }
          />
        </label>

        <label>
          Trainingsart
          <select
            value={filters.type}
            onChange={(event) =>
              onChange({
                ...filters,
                type: event.target.value as TrainingType | '',
              })
            }
          >
            <option value="">Alle Trainingsarten</option>
            <option value="RUNNING">Laufen</option>
            <option value="BOULDERING">Bouldern</option>
            <option value="STRENGTH">Krafttraining</option>
          </select>
        </label>

        <label>
          Von
          <input
            type="date"
            value={filters.from}
            max={filters.to || undefined}
            onChange={(event) =>
              onChange({
                ...filters,
                from: event.target.value,
              })
            }
          />
        </label>

        <label>
          Bis
          <input
            type="date"
            value={filters.to}
            min={filters.from || undefined}
            onChange={(event) =>
              onChange({
                ...filters,
                to: event.target.value,
              })
            }
          />
        </label>

        <div className="filter-actions">
          <button type="submit" disabled={loading}>
            {loading ? 'Sucht …' : 'Filter anwenden'}
          </button>

          <button
            className="filter-reset-button"
            type="button"
            disabled={loading}
            onClick={onReset}
          >
            Zurücksetzen
          </button>
        </div>
      </form>
    </section>
  )
}