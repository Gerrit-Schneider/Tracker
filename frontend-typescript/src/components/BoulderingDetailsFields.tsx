import {
  boulderingGrades,
  type BoulderingGrade,
  type BoulderingGradeResultInput,
} from '../types/training'

interface BoulderingDetailsFieldsProps {
  value: BoulderingGradeResultInput[]
  onChange: (value: BoulderingGradeResultInput[]) => void
}

export function BoulderingDetailsFields({
  value,
  onChange,
}: BoulderingDetailsFieldsProps) {
  function addGrade() {
    const nextGrade = boulderingGrades.find(
      (grade) =>
        !value.some((result) => result.grade === grade),
    )

    if (!nextGrade) {
      return
    }

    onChange([
      ...value,
      {
        grade: nextGrade,
        attemptedCount: 1,
        completedCount: 1,
      },
    ])
  }

  function updateGrade(
    index: number,
    changes: Partial<BoulderingGradeResultInput>,
  ) {
    onChange(
      value.map((result, resultIndex) =>
        resultIndex === index
          ? { ...result, ...changes }
          : result,
      ),
    )
  }

  function removeGrade(index: number) {
    onChange(
      value.filter((_, resultIndex) => resultIndex !== index),
    )
  }

  return (
    <fieldset className="detail-panel field-wide">
      <legend>Bouldering-Details</legend>

      {value.length === 0 && (
        <p className="detail-hint">
          Füge einen Schwierigkeitsgrad hinzu.
        </p>
      )}

      <div className="repeating-list">
        {value.map((result, index) => (
          <div
            className="repeating-row bouldering-row"
            key={`${result.grade}-${index}`}
          >
            <label>
              Grad
              <select
                value={result.grade}
                onChange={(event) =>
                  updateGrade(index, {
                    grade: event.target
                      .value as BoulderingGrade,
                  })
                }
              >
                {boulderingGrades.map((grade) => (
                  <option
                    key={grade}
                    value={grade}
                    disabled={value.some(
                      (otherResult, otherIndex) =>
                        otherIndex !== index &&
                        otherResult.grade === grade,
                    )}
                  >
                    {grade}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Versuche
              <input
                type="number"
                min="0"
                value={result.attemptedCount}
                required
                onChange={(event) => {
                  const attemptedCount = Number(
                    event.target.value,
                  )

                  updateGrade(index, {
                    attemptedCount,
                    completedCount: Math.min(
                      result.completedCount,
                      attemptedCount,
                    ),
                  })
                }}
              />
            </label>

            <label>
              Geschafft
              <input
                type="number"
                min="0"
                max={result.attemptedCount}
                value={result.completedCount}
                required
                onChange={(event) =>
                  updateGrade(index, {
                    completedCount: Number(
                      event.target.value,
                    ),
                  })
                }
              />
            </label>

            <button
              className="remove-detail-button"
              type="button"
              onClick={() => removeGrade(index)}
            >
              Entfernen
            </button>
          </div>
        ))}
      </div>

      <button
        className="secondary-button add-detail-button"
        type="button"
        disabled={value.length === boulderingGrades.length}
        onClick={addGrade}
      >
        Grad hinzufügen
      </button>
    </fieldset>
  )
}