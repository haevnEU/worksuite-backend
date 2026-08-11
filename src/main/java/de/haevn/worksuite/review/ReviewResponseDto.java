package de.haevn.worksuite.review;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Data transfer object returned by review management endpoints.
 *
 * <p>Example usage:
 * <pre>{@code
 * ReviewResponseDto response = ReviewResponseDto.fromRecord(reviewRecord);
 * }</pre>
 *
 * @param id primary unique identifier
 * @param ticketNumber associated ticket number
 * @param title review topic title
 * @param description contextual explanation
 * @param type presentation classification format
 * @param demoNotes parsed live demonstration guide
 * @param keyFacts parsed presentation bullet points
 * @param isArchived whether this item has been archived
 * @param createdAt creation instant
 */
@Schema(description = "Detailed review item response representation")
public record ReviewResponseDto(

    @Schema(description = "Unique review identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID id,

    @Schema(description = "Associated ticket number", example = "FEAT-2048") String ticketNumber,

    @Schema(description = "Title of the review item", example = "Single Sign-On Integration") String title,

    @Schema(description = "Description or background summary",
        example = "Integration of central OIDC identity provider.") String description,

    @Schema(description = "Format category of the review", example = "PRESENTATION") ReviewType type,

    @Schema(description = "Demonstration steps and notes for live presentations",
        example = "Open login screen, select SSO provider, verify JWT claim extraction.") String demoNotes,

    @ArraySchema(schema = @Schema(description = "Bullet points for presentation reviews",
        example = "Supports standard OpenID Connect discovery")) List<String> keyFacts,

    @Schema(description = "Whether the review item is archived", example = "false") boolean isArchived,

    @Schema(description = "Timestamp when the review was created",
        example = "2026-08-17T18:45:00.000Z") Instant createdAt) {

    /**
     * Maps an internal {@link ReviewRecord} to an external {@link ReviewResponseDto}.
     *
     * <p>Example:
     * <pre>{@code
     * ReviewResponseDto dto = ReviewResponseDto.fromRecord(record);
     * }</pre>
     *
     * @param record the source {@link ReviewRecord}
     * @return the transformed {@link ReviewResponseDto}
     */
    public static ReviewResponseDto fromRecord(final ReviewRecord record) {
        Objects.requireNonNull(record, "ReviewRecord must not be null");
        return new ReviewResponseDto(record.id(), record.ticketNumber(), record.title(), record.description(),
            record.type(), record.getDemoNotes(), record.getKeyFacts(), record.isArchived(), record.createdAt());
    }
}