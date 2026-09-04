import type {
  CreateTrainingSession,
  TrainingSession,
  TrainingSessionPage,
  TrainingSessionSearchParams,
} from '../types/training'

const endpoint = '/api/training-sessions'

export async function getTrainingSessions(): Promise<
  TrainingSession[]
> {
  const response = await fetch(endpoint)

  if (!response.ok) {
    throw new Error(
      'Trainingseinheiten konnten nicht geladen werden.',
    )
  }

  return response.json() as Promise<TrainingSession[]>
}

export async function searchTrainingSessions(
  parameters: TrainingSessionSearchParams,
): Promise<TrainingSessionPage> {
  const searchParameters = new URLSearchParams()

  if (parameters.type) {
    searchParameters.set('type', parameters.type)
  }

  if (parameters.from) {
    searchParameters.set('from', parameters.from)
  }

  if (parameters.to) {
    searchParameters.set('to', parameters.to)
  }

  if (parameters.query?.trim()) {
    searchParameters.set('query', parameters.query.trim())
  }

  searchParameters.set(
    'page',
    String(parameters.page ?? 0),
  )
  searchParameters.set(
    'size',
    String(parameters.size ?? 10),
  )

  const response = await fetch(
    `${endpoint}/search?${searchParameters.toString()}`,
  )

  if (!response.ok) {
    throw new Error(
      'Gefilterte Trainingseinheiten konnten nicht geladen werden.',
    )
  }

  return response.json() as Promise<TrainingSessionPage>
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
    throw new Error(
      'Trainingseinheit konnte nicht gespeichert werden.',
    )
  }

  return response.json() as Promise<TrainingSession>
}

export async function deleteTrainingSession(
  id: number,
): Promise<void> {
  const response = await fetch(`${endpoint}/${id}`, {
    method: 'DELETE',
  })

  if (!response.ok) {
    throw new Error(
      'Trainingseinheit konnte nicht gelöscht werden.',
    )
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
    throw new Error(
      'Trainingseinheit konnte nicht aktualisiert werden.',
    )
  }

  return response.json() as Promise<TrainingSession>
}

export async function downloadTrainingSessionsCsv():
  Promise<Blob> {
  const response = await fetch(`${endpoint}/export.csv`)

  if (!response.ok) {
    throw new Error(
      'Der CSV-Export konnte nicht heruntergeladen werden.',
    )
  }

  return response.blob()
}

export interface TrainingSessionCsvImportResult {
  importedSessions: number
  processedRows: number
}

export async function importTrainingSessionsCsv(
  file: File,
): Promise<TrainingSessionCsvImportResult> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(`${endpoint}/import.csv`, {
    method: 'POST',
    body: formData,
  })

  if (!response.ok) {
    let message =
      'Die CSV-Datei konnte nicht importiert werden.'

    try {
      const errorResponse = (await response.json()) as {
        detail?: string
        message?: string
      }

      message =
        errorResponse.detail ??
        errorResponse.message ??
        message
    } catch {
      // Die Standardmeldung bleibt erhalten.
    }

    throw new Error(message)
  }

  return response.json() as Promise<TrainingSessionCsvImportResult>
}