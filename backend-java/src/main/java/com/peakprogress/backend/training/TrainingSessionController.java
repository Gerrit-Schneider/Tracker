package com.peakprogress.backend.training;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-sessions")
public class TrainingSessionController {

    private final TrainingSessionService service;

    public TrainingSessionController(
            TrainingSessionService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingSessionResponse create(
            @Valid @RequestBody CreateTrainingSessionRequest request
    ) {
        return service.create(request);
    }

    @GetMapping
    public List<TrainingSessionResponse> findAll(
            @RequestParam(required = false) TrainingType type
    ) {
        return service.findAll(type);
    }
}