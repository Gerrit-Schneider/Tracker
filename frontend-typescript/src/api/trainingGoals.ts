import type {
  SaveTrainingGoal,
  TrainingGoal,
} from '../types/goal'

const endpoint = '/api/training-goals'

async function readError(
  response: Response,
  fallbackMessage: string,
): Promise<Error> {
  try {
    const result = (await response.json()) as {
      detail?: string
      message?: string
    }

    return new Error(
      result.detail ?? result.message ?? fallbackMessage,
    )
  } catch {
    return new Error(fallbackMessage)
  }
}

export async function getTrainingGoals():
  Promise<TrainingGoal[]> {
  const response = await fetch(endpoint)

  if (!response.ok) {
    throw await readError(
      response,
      'Trainingsziele konnten nicht geladen werden.',
    )
  }

  return response.json() as Promise<TrainingGoal[]>
}

export async function createTrainingGoal(
  goal: SaveTrainingGoal,
): Promise<TrainingGoal> {
  const response = await fetch(endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(goal),
  })

  if (!response.ok) {
    throw await readError(
      response,
      'Trainingsziel konnte nicht gespeichert werden.',
    )
  }

  return response.json() as Promise<TrainingGoal>
}

export async function updateTrainingGoal(
  id: number,
  goal: SaveTrainingGoal,
): Promise<TrainingGoal> {
  const response = await fetch(`${endpoint}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(goal),
  })

  if (!response.ok) {
    throw await readError(
      response,
      'Trainingsziel konnte nicht aktualisiert werden.',
    )
  }

  return response.json() as Promise<TrainingGoal>
}

export async function deleteTrainingGoal(
  id: number,
): Promise<void> {
  const response = await fetch(`${endpoint}/${id}`, {
    method: 'DELETE',
  })

  if (!response.ok) {
    throw await readError(
      response,
      'Trainingsziel konnte nicht gelöscht werden.',
    )
  }
}