package de.haevn.worksuite.review;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record ReviewRecord(UUID id, String ticketNumber, String title, String description, ReviewType type,
                           String content, // Kombiniertes Feld
                           boolean isArchived, Instant createdAt) {
    public ReviewRecord {
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new IllegalArgumentException("Ticket number must not be empty");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("ReviewType must not be null");
        }
    }

    public static String joinKeyFacts(List<String> keyFacts) {
        if (keyFacts == null || keyFacts.isEmpty()) {
            return "";
        }
        return String.join("\n", keyFacts);
    }

    public String getDemoNotes() {
        return content != null ? content : "";
    }

    public List<String> getKeyFacts() {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        return Arrays.stream(content.split("\r?\n")).map(String::trim).filter(line -> !line.isEmpty()).toList();
    }
}