export type TrainingType =
  | 'RUNNING'
  | 'BOULDERING'
  | 'STRENGTH'

export const runTypes = [
  'EASY',
  'TEMPO',
  'INTERVAL',
  'LONG_RUN',
  'RECOVERY',
  'RACE',
  'OTHER',
] as const

export type RunType = (typeof runTypes)[number]

export const boulderingGrades = [
  'VB',
  'V0',
  'V1',
  'V2',
  'V3',
  'V4',
  'V5',
  'V6',
  'V7',
  'V8',
  'V9',
  'V10',
  'V11',
  'V12',
  'V13',
  'V14',
  'V15',
  'V16',
  'V17',
] as const

export type BoulderingGrade =
  (typeof boulderingGrades)[number]

export interface RunningDetails {
  runType: RunType
  distanceMeters: number
  elapsedSeconds: number
  paceSecondsPerKilometer: number
  averageHeartRate: number | null
  maxHeartRate: number | null
}

export interface RunningDetailsInput {
  runType: RunType
  distanceMeters: number
  elapsedSeconds: number
  averageHeartRate: number | null
  maxHeartRate: number | null
}

export interface BoulderingGradeResult {
  id: number
  grade: BoulderingGrade
  attemptedCount: number
  completedCount: number
}

export interface BoulderingGradeResultInput {
  grade: BoulderingGrade
  attemptedCount: number
  completedCount: number
}

export interface StrengthSet {
  id: number
  setNumber: number
  repetitions: number
  weightKg: number
  volumeKg: number
}

export interface StrengthSetInput {
  repetitions: number
  weightKg: number
}

export interface StrengthExercise {
  id: number
  exerciseName: string
  exerciseOrder: number
  sets: StrengthSet[]
  volumeKg: number
}

export interface StrengthExerciseInput {
  exerciseName: string
  sets: StrengthSetInput[]
}

export interface TrainingSession {
  id: number
  type: TrainingType
  trainingDate: string
  durationMinutes: number
  notes: string | null
  runningDetails: RunningDetails | null
  boulderingResults: BoulderingGradeResult[]
  strengthExercises: StrengthExercise[]
}

export interface CreateTrainingSession {
  type: TrainingType
  trainingDate: string
  durationMinutes: number
  notes: string
  runningDetails?: RunningDetailsInput | null
  boulderingResults?: BoulderingGradeResultInput[]
  strengthExercises?: StrengthExerciseInput[]
}

export type UpdateTrainingSession = CreateTrainingSession

export interface TrainingSessionPage {
  content: TrainingSession[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface TrainingSessionSearchParams {
  type?: TrainingType
  from?: string
  to?: string
  query?: string
  page?: number
  size?: number
}