package com.peakprogress.backend.training.csv;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/training-sessions")
public class TrainingSessionCsvController {

    private final TrainingSessionCsvExportService exportService;
    private final TrainingSessionCsvImportService importService;

    public TrainingSessionCsvController(
            TrainingSessionCsvExportService exportService,
            TrainingSessionCsvImportService importService
    ) {
        this.exportService = exportService;
        this.importService = importService;
    }

    @GetMapping(
            value = "/export.csv",
            produces = "text/csv"
    )
    public ResponseEntity<byte[]> export() {
        byte[] csv = exportService.export();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"peak-progress-training.csv\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                "text/csv;charset=UTF-8"
                        )
                )
                .body(csv);
    }

    @PostMapping(
            value = "/import.csv",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public TrainingSessionCsvImportResponse importCsv(
            @RequestParam("file") MultipartFile file
    ) {
        return importService.importCsv(file);
    }
}