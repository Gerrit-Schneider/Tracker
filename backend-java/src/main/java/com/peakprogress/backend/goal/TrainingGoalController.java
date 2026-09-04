package com.peakprogress.backend.goal;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-goals")
public class TrainingGoalController {

    private final TrainingGoalService service;

    public TrainingGoalController(TrainingGoalService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingGoalResponse create(
            @Valid @RequestBody CreateTrainingGoalRequest request
    ) {
        return service.create(request);
    }

    @GetMapping
    public List<TrainingGoalResponse> findAll() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public TrainingGoalResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTrainingGoalRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}