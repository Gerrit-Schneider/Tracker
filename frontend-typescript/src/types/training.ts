export type TrainingType =
  | 'RUNNING'
  | 'BOULDERING'
  | 'STRENGTH'

export interface TrainingSession {
  id: number
  type: TrainingType
  trainingDate: string
  durationMinutes: number
  notes: string | null
}

export interface CreateTrainingSession {
  type: TrainingType
  trainingDate: string
  durationMinutes: number
  notes: string
}