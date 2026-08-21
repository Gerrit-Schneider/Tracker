import type {
  CreateTrainingSession,
  TrainingSession,
} from '../types/training'

const endpoint = '/api/training-sessions'

export async function getTrainingSessions():
  Promise<TrainingSession[]> {
  const response = await fetch(endpoint)

  if (!response.ok) {
    throw new Error('Trainingseinheiten konnten nicht geladen werden.')
  }

  return response.json()
}

export async function createTrainingSession(
  session: CreateTrainingSession,
): Promise<TrainingSession> {
  const response = await fetch(endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(session),
  })

  if (!response.ok) {
    throw new Error('Trainingseinheit konnte nicht gespeichert werden.')
  }

  return response.json()
}
export async function deleteTrainingSession(
  id: number,
): Promise<void> {
  const response = await fetch(`${endpoint}/${id}`, {
    method: 'DELETE',
  })

  if (!response.ok) {
    throw new Error('Trainingseinheit konnte nicht gelöscht werden.')
  }
}
export async function updateTrainingSession(
  id: number,
  session: CreateTrainingSession,
): Promise<TrainingSession> {
  const response = await fetch(`${endpoint}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(session),
  })

  if (!response.ok) {
    throw new Error('Trainingseinheit konnte nicht aktualisiert werden.')
  }

  return response.json()
}