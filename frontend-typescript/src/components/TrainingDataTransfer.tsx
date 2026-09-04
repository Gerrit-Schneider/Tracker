import {
  useRef,
  useState,
} from 'react'
import type { ChangeEvent } from 'react'
import {
  downloadTrainingSessionsCsv,
  importTrainingSessionsCsv,
} from '../api/trainingSessions'

interface TrainingDataTransferProps {
  onImported: () => void
}

export function TrainingDataTransfer({
  onImported,
}: TrainingDataTransferProps) {
  const fileInput = useRef<HTMLInputElement>(null)

  const [selectedFile, setSelectedFile] =
    useState<File | null>(null)
  const [downloading, setDownloading] = useState(false)
  const [importing, setImporting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  function handleFileChange(
    event: ChangeEvent<HTMLInputElement>,
  ) {
    setSelectedFile(event.target.files?.[0] ?? null)
    setError(null)
    setSuccess(null)
  }

  async function handleExport() {
    setDownloading(true)
    setError(null)
    setSuccess(null)

    try {
      const csv = await downloadTrainingSessionsCsv()
      const downloadUrl = URL.createObjectURL(csv)
      const downloadLink = document.createElement('a')
      const currentDate = new Date()
        .toISOString()
        .slice(0, 10)

      downloadLink.href = downloadUrl
      downloadLink.download =
        `peak-progress-training-${currentDate}.csv`

      document.body.appendChild(downloadLink)
      downloadLink.click()
      downloadLink.remove()

      URL.revokeObjectURL(downloadUrl)
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Der CSV-Export ist fehlgeschlagen.',
      )
    } finally {
      setDownloading(false)
    }
  }

  async function handleImport() {
    if (!selectedFile) {
      setError('Bitte wähle zuerst eine CSV-Datei aus.')
      return
    }

    setImporting(true)
    setError(null)
    setSuccess(null)

    try {
      const result = await importTrainingSessionsCsv(
        selectedFile,
      )

      const sessionLabel =
        result.importedSessions === 1
          ? 'Trainingseinheit'
          : 'Trainingseinheiten'

      setSuccess(
        `${result.importedSessions} ${sessionLabel} `
          + `aus ${result.processedRows} CSV-Zeilen importiert.`,
      )

      setSelectedFile(null)

      if (fileInput.current) {
        fileInput.current.value = ''
      }

      onImported()
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Der CSV-Import ist fehlgeschlagen.',
      )
    } finally {
      setImporting(false)
    }
  }

  const busy = downloading || importing

  return (
    <section className="data-transfer-section">
      <div>
        <p className="eyebrow">Datenaustausch</p>
        <h2>CSV-Import und -Export</h2>

        <p className="data-transfer-description">
          Sichere deine vollständigen Trainingsdaten oder
          importiere zuvor exportierte PeakProgress-Dateien.
        </p>
      </div>

      <div className="data-transfer-actions">
        <button
          className="export-button"
          type="button"
          disabled={busy}
          onClick={() => void handleExport()}
        >
          {downloading
            ? 'Export wird erstellt …'
            : 'CSV herunterladen'}
        </button>

        <div className="import-controls">
          <label className="csv-file-label">
            CSV-Datei auswählen
            <input
              ref={fileInput}
              type="file"
              accept=".csv,text/csv"
              disabled={busy}
              onChange={handleFileChange}
            />
          </label>

          {selectedFile && (
            <span className="selected-file">
              {selectedFile.name}
            </span>
          )}

          <button
            className="import-button"
            type="button"
            disabled={busy || !selectedFile}
            onClick={() => void handleImport()}
          >
            {importing
              ? 'Wird importiert …'
              : 'CSV importieren'}
          </button>
        </div>
      </div>

      {error && (
        <p className="form-error transfer-message">
          {error}
        </p>
      )}

      {success && (
        <p className="import-success transfer-message">
          {success}
        </p>
      )}
    </section>
  )
}