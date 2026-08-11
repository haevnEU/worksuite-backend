package de.haevn.worksuite.review;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewResponseDto(UUID id, String ticketNumber, String title, String description, ReviewType type,
                                String demoNotes, List<String> keyFacts, boolean isArchived, Instant createdAt) {
    public static ReviewResponseDto fromRecord(ReviewRecord record) {
        return new ReviewResponseDto(record.id(), record.ticketNumber(), record.title(), record.description(),
            record.type(), record.getDemoNotes(), record.getKeyFacts(), record.isArchived(), record.createdAt());
    }
}