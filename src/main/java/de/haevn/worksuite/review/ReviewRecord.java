package de.haevn.worksuite.review;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain-level immutable record representing a review entry with extracted helper representations.
 *
 * <p>Example usage:
 * <pre>{@code
 * ReviewRecord record = new ReviewRecord(
 *     UUID.randomUUID(),
 *     "BUG-512",
 *     "Fix Null Pointer on Export",
 *     "Handled empty collections in export flow.",
 *     ReviewType.DEMO,
 *     "Step 1: Download empty report",
 *     false,
 *     Instant.now()
 * );
 * List<String> facts = record.getKeyFacts();
 * }</pre>
 *
 * @param id primary unique identifier
 * @param ticketNumber associated ticket key
 * @param title review topic title
 * @param description contextual explanation
 * @param type presentation classification format
 * @param content unified raw content representation
 * @param isArchived whether this item has been archived
 * @param createdAt creation instant
 */
public record ReviewRecord(UUID id, String ticketNumber, String title, String description, ReviewType type,
                           String content, boolean isArchived, Instant createdAt) {

    /**
     * Compact constructor validating required domain invariants.
     */
    public ReviewRecord {
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new IllegalArgumentException("Ticket number must not be null or blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be null or blank");
        }
        Objects.requireNonNull(type, "ReviewType must not be null");
    }

    /**
     * Utility method to join key fact strings into a single newline-separated text block.
     *
     * <p>Example usage:
     * <pre>{@code
     * String joined = ReviewRecord.joinKeyFacts(List.of("Point A", "Point B"));
     * }</pre>
     *
     * @param keyFacts list of strings to concatenate
     * @return newline-separated text
     */
    public static String joinKeyFacts(final List<String> keyFacts) {
        if (keyFacts == null || keyFacts.isEmpty()) {
            return "";
        }
        return keyFacts.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty())
            .collect(Collectors.joining("\n"));
    }

    /**
     * Extracts demonstration notes from the internal content string.
     *
     * @return demonstration notes text
     */
    public String getDemoNotes() {
        return content != null ? content : "";
    }

    /**
     * Parses newline-delimited key facts from the unified internal content string.
     *
     * @return list of non-blank key fact lines
     */
    public List<String> getKeyFacts() {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        return Arrays.stream(content.split("\\R")).map(String::trim).filter(line -> !line.isEmpty()).toList();
    }
}