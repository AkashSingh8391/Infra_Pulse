package com.infrapulse.backend.controller;

import com.infrapulse.backend.dto.ai.*;
import com.infrapulse.backend.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/suggest-category")
    public ResponseEntity<CategorySuggestionResponse> suggestCategory(@Valid @RequestBody DescriptionRequest request) {
        return ResponseEntity.ok(aiService.suggestCategory(request.description()));
    }

    @PostMapping("/predict-priority")
    public ResponseEntity<PriorityPredictionResponse> predictPriority(@Valid @RequestBody DescriptionRequest request) {
        return ResponseEntity.ok(aiService.predictPriority(request.description()));
    }

    @PostMapping("/check-duplicate")
    public ResponseEntity<DuplicateCheckResponse> checkDuplicate(@Valid @RequestBody DescriptionRequest request) {
        return ResponseEntity.ok(aiService.checkDuplicate(request.description()));
    }

    @PostMapping("/generate-title")
    public ResponseEntity<TitleSuggestionResponse> generateTitle(@Valid @RequestBody DescriptionRequest request) {
        return ResponseEntity.ok(aiService.generateTitle(request.description()));
    }

    @PostMapping("/improve-description")
    public ResponseEntity<DescriptionImprovementResponse> improveDescription(@Valid @RequestBody DescriptionRequest request) {
        return ResponseEntity.ok(aiService.improveDescription(request.description()));
    }
}
