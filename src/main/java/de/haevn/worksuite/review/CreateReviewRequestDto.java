package de.haevn.worksuite.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateReviewRequestDto(@NotBlank String ticketNumber, @NotBlank String title, String description,
                                     @NotNull ReviewType type, String demoNotes, List<String> keyFacts) {
    public String toContentString() {
        if (type == ReviewType.DEMO) {
            return demoNotes != null ? demoNotes : "";
        }
        if (type == ReviewType.PRESENTATION && keyFacts != null && !keyFacts.isEmpty()) {
            return String.join("\n", keyFacts.stream().map(String::trim).filter(s -> !s.isEmpty()).toList());
        }
        return "";
    }
}