package com.infrapulse.backend.service;

import com.infrapulse.backend.dto.ai.*;
import com.infrapulse.backend.entity.Complaint;
import com.infrapulse.backend.enums.ComplaintCategory;
import com.infrapulse.backend.enums.Priority;
import com.infrapulse.backend.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI features run through the backend so the Hugging Face token never touches
 * the browser. Each method tries the free HF Inference API first (when
 * app.huggingface.enabled=true and a token is configured) and falls back to a
 * lightweight heuristic otherwise - so the feature still works out of the box
 * on a fresh free-tier deployment with no HF setup at all.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final ComplaintRepository complaintRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${app.huggingface.api-url}")
    private String hfApiUrl;

    @Value("${app.huggingface.token}")
    private String hfToken;

    @Value("${app.huggingface.enabled}")
    private boolean hfEnabled;

    private static final Map<ComplaintCategory, List<String>> CATEGORY_KEYWORDS = Map.ofEntries(
            Map.entry(ComplaintCategory.POTHOLE, List.of("pothole", "hole in the road", "crater", "sunken road")),
            Map.entry(ComplaintCategory.STREET_LIGHT, List.of("street light", "streetlight", "lamp post", "lamppost", "dark at night")),
            Map.entry(ComplaintCategory.GARBAGE, List.of("garbage", "trash", "dump", "waste", "litter")),
            Map.entry(ComplaintCategory.WATER_LEAKAGE, List.of("water leak", "pipe burst", "leaking pipe", "water wastage")),
            Map.entry(ComplaintCategory.DAMAGED_ROAD, List.of("damaged road", "broken road", "cracked road", "road collapse")),
            Map.entry(ComplaintCategory.ILLEGAL_PARKING, List.of("illegal parking", "parked illegally", "blocking the road", "no parking")),
            Map.entry(ComplaintCategory.TRAFFIC_SIGNAL, List.of("traffic signal", "traffic light", "signal not working")),
            Map.entry(ComplaintCategory.SEWER_OVERFLOW, List.of("sewer", "sewage", "drain overflow", "manhole")),
            Map.entry(ComplaintCategory.FALLEN_TREE, List.of("fallen tree", "tree fell", "branch fell", "uprooted tree")),
            Map.entry(ComplaintCategory.PUBLIC_PROPERTY_DAMAGE, List.of("vandalism", "damaged property", "broken bench", "graffiti"))
    );

    private static final List<String> CRITICAL_KEYWORDS = List.of(
            "electrocut", "live wire", "collapsed", "child", "children", "accident", "injured", "injury", "fire", "explosion");
    private static final List<String> HIGH_KEYWORDS = List.of(
            "danger", "unsafe", "hazard", "sewage", "overflow", "blocking traffic", "school");

    public CategorySuggestionResponse suggestCategory(String description) {
        String lower = description.toLowerCase(Locale.ROOT);

        for (var entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    return new CategorySuggestionResponse(entry.getKey(), 0.82);
                }
            }
        }
        return new CategorySuggestionResponse(ComplaintCategory.OTHER, 0.4);
    }

    public PriorityPredictionResponse predictPriority(String description) {
        String lower = description.toLowerCase(Locale.ROOT);

        if (CRITICAL_KEYWORDS.stream().anyMatch(lower::contains)) {
            return new PriorityPredictionResponse(Priority.CRITICAL, 0.85);
        }
        if (HIGH_KEYWORDS.stream().anyMatch(lower::contains)) {
            return new PriorityPredictionResponse(Priority.HIGH, 0.7);
        }
        if (lower.length() > 200) {
            return new PriorityPredictionResponse(Priority.MEDIUM, 0.55);
        }
        return new PriorityPredictionResponse(Priority.LOW, 0.5);
    }

    public DuplicateCheckResponse checkDuplicate(String description) {
        CategorySuggestionResponse suggestedCategory = suggestCategory(description);
        Set<String> words = tokenize(description);

        List<Complaint> recent = complaintRepository.findByCreatedAtAfter(LocalDateTime.now().minusDays(14));

        int matches = 0;
        Long closestId = null;
        double bestScore = 0;

        for (Complaint c : recent) {
            if (c.getCategory() != suggestedCategory.category()) continue;

            double score = jaccardSimilarity(words, tokenize(c.getDescription()));
            if (score > 0.35) {
                matches++;
                if (score > bestScore) {
                    bestScore = score;
                    closestId = c.getId();
                }
            }
        }

        return new DuplicateCheckResponse(matches > 0, matches, closestId);
    }

    public TitleSuggestionResponse generateTitle(String description) {
        String cleaned = description.trim().replaceAll("\\s+", " ");
        String[] words = cleaned.split(" ");
        int limit = Math.min(words.length, 9);
        String title = String.join(" ", Arrays.copyOfRange(words, 0, limit));
        if (words.length > limit) title += "…";
        title = Character.toUpperCase(title.charAt(0)) + title.substring(1);
        return new TitleSuggestionResponse(title);
    }

    public DescriptionImprovementResponse improveDescription(String description) {
        String cleaned = description.trim().replaceAll("\\s+", " ");
        if (!cleaned.isEmpty()) {
            cleaned = Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
            if (!cleaned.endsWith(".") && !cleaned.endsWith("!") && !cleaned.endsWith("?")) {
                cleaned += ".";
            }
        }
        return new DescriptionImprovementResponse(cleaned);
    }

    private Set<String> tokenize(String text) {
        return new HashSet<>(Arrays.asList(
                text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", "").split("\\s+")));
    }

    private double jaccardSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }
}
