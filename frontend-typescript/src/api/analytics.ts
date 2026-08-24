export interface AnalyticsSummary {
  totalSessions: number
  totalDurationMinutes: number
  averageDurationMinutes: number
  sessionsByType: Record<string, number>
  durationByType: Record<string, number>
}

const ANALYTICS_API_URL =
  import.meta.env.VITE_ANALYTICS_API_URL ?? 'http://localhost:8000'

export async function getAnalyticsSummary(): Promise<AnalyticsSummary> {
  const response = await fetch(
    `${ANALYTICS_API_URL}/api/analytics/summary`,
  )

  if (!response.ok) {
    throw new Error(
      `Analytics request failed with status ${response.status}`,
    )
  }

  return response.json() as Promise<AnalyticsSummary>
}
