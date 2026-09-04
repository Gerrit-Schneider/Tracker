export const goalMetrics = [
  'RUNNING_DISTANCE_KM',
  'RUNNING_PACE_SECONDS_PER_KM',
  'BOULDERING_GRADE',
  'STRENGTH_WEIGHT_KG',
] as const

export type GoalMetric = (typeof goalMetrics)[number]

export interface TrainingGoal {
  id: number
  title: string
  metric: GoalMetric
  targetValue: number
  exerciseName: string | null
  targetDate: string | null
  createdAt: string
  currentValue: number | null
  progressPercent: number
  completed: boolean
}

export interface SaveTrainingGoal {
  title: string
  metric: GoalMetric
  targetValue: number
  exerciseName: string | null
  targetDate: string | null
}