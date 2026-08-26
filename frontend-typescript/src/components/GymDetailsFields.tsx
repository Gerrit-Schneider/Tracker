import type {
  StrengthExerciseInput,
  StrengthSetInput,
} from '../types/training'

interface StrengthDetailsFieldsProps {
  value: StrengthExerciseInput[]
  onChange: (value: StrengthExerciseInput[]) => void
}

export function StrengthDetailsFields({
  value,
  onChange,
}: StrengthDetailsFieldsProps) {
  function addExercise() {
    onChange([
      ...value,
      {
        exerciseName: '',
        sets: [
          {
            repetitions: 5,
            weightKg: 0,
          },
        ],
      },
    ])
  }

  function updateExerciseName(
    exerciseIndex: number,
    exerciseName: string,
  ) {
    onChange(
      value.map((exercise, index) =>
        index === exerciseIndex
          ? { ...exercise, exerciseName }
          : exercise,
      ),
    )
  }

  function removeExercise(exerciseIndex: number) {
    onChange(
      value.filter((_, index) => index !== exerciseIndex),
    )
  }

  function addSet(exerciseIndex: number) {
    onChange(
      value.map((exercise, index) => {
        if (index !== exerciseIndex) {
          return exercise
        }

        const previousSet =
          exercise.sets[exercise.sets.length - 1]

        return {
          ...exercise,
          sets: [
            ...exercise.sets,
            {
              repetitions: previousSet?.repetitions ?? 5,
              weightKg: previousSet?.weightKg ?? 0,
            },
          ],
        }
      }),
    )
  }

  function updateSet(
    exerciseIndex: number,
    setIndex: number,
    changes: Partial<StrengthSetInput>,
  ) {
    onChange(
      value.map((exercise, index) =>
        index === exerciseIndex
          ? {
              ...exercise,
              sets: exercise.sets.map(
                (strengthSet, currentSetIndex) =>
                  currentSetIndex === setIndex
                    ? { ...strengthSet, ...changes }
                    : strengthSet,
              ),
            }
          : exercise,
      ),
    )
  }

  function removeSet(
    exerciseIndex: number,
    setIndex: number,
  ) {
    onChange(
      value.map((exercise, index) =>
        index === exerciseIndex
          ? {
              ...exercise,
              sets: exercise.sets.filter(
                (_, currentSetIndex) =>
                  currentSetIndex !== setIndex,
              ),
            }
          : exercise,
      ),
    )
  }

  return (
    <fieldset className="detail-panel field-wide">
      <legend>Krafttraining-Details</legend>

      {value.length === 0 && (
        <p className="detail-hint">
          Füge deine erste Übung hinzu.
        </p>
      )}

      <div className="exercise-list">
        {value.map((exercise, exerciseIndex) => {
          const volumeKg = exercise.sets.reduce(
            (total, strengthSet) =>
              total +
              strengthSet.repetitions *
                strengthSet.weightKg,
            0,
          )

          return (
            <article
              className="exercise-editor"
              key={exerciseIndex}
            >
              <div className="exercise-heading">
                <label>
                  Übung
                  <input
                    type="text"
                    maxLength={100}
                    placeholder="z. B. Kreuzheben"
                    value={exercise.exerciseName}
                    required
                    onChange={(event) =>
                      updateExerciseName(
                        exerciseIndex,
                        event.target.value,
                      )
                    }
                  />
                </label>

                <button
                  className="remove-detail-button"
                  type="button"
                  onClick={() =>
                    removeExercise(exerciseIndex)
                  }
                >
                  Übung entfernen
                </button>
              </div>

              <div className="set-list">
                {exercise.sets.map(
                  (strengthSet, setIndex) => (
                    <div className="set-row" key={setIndex}>
                      <strong>Satz {setIndex + 1}</strong>

                      <label>
                        Wiederholungen
                        <input
                          type="number"
                          min="1"
                          value={
                            strengthSet.repetitions
                          }
                          required
                          onChange={(event) =>
                            updateSet(
                              exerciseIndex,
                              setIndex,
                              {
                                repetitions: Number(
                                  event.target.value,
                                ),
                              },
                            )
                          }
                        />
                      </label>

                      <label>
                        Gewicht / Zusatzgewicht (kg)
                        <input
                          type="number"
                          min="0"
                          max="9999.99"
                          step="0.25"
                          value={strengthSet.weightKg}
                          required
                          onChange={(event) =>
                            updateSet(
                              exerciseIndex,
                              setIndex,
                              {
                                weightKg: Number(
                                  event.target.value,
                                ),
                              },
                            )
                          }
                        />
                      </label>

                      <button
                        className="remove-detail-button"
                        type="button"
                        disabled={
                          exercise.sets.length === 1
                        }
                        onClick={() =>
                          removeSet(
                            exerciseIndex,
                            setIndex,
                          )
                        }
                      >
                        Satz entfernen
                      </button>
                    </div>
                  ),
                )}
              </div>

              <div className="exercise-footer">
                <button
                  className="secondary-button"
                  type="button"
                  onClick={() => addSet(exerciseIndex)}
                >
                  Satz hinzufügen
                </button>

                <span>
                  Volumen: {volumeKg.toLocaleString('de-DE')}{' '}
                  kg
                </span>
              </div>
            </article>
          )
        })}
      </div>

      <button
        className="secondary-button add-detail-button"
        type="button"
        disabled={value.length >= 50}
        onClick={addExercise}
      >
        Übung hinzufügen
      </button>
    </fieldset>
  )
}