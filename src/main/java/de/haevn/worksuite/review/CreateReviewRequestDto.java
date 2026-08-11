package de.haevn.worksuite.review;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Request data transfer object for creating or updating a sprint review entry.
 *
 * <p>Example usage:
 * <pre>{@code
 * CreateReviewRequestDto request = new CreateReviewRequestDto(
 *     "PROJ-1024",
 *     "Single Sign-On Integration",
 *     "Integration of central OIDC identity provider.",
 *     ReviewType.PRESENTATION,
 *     null,
 *     List.of("JWT verification enabled", "Roles propagated via claims")
 * );
 * String content = request.toContentString();
 * }</pre>
 *
 * @param ticketNumber reference identifier of the associated ticket
 * @param title headline or summary of the review item
 * @param description detailed background or summary text
 * @param type the review classification format ({@link ReviewType#DEMO} or {@link ReviewType#PRESENTATION})
 * @param demoNotes markdown or raw notes for live system demonstrations
 * @param keyFacts bullet-point takeaways and discussion points for presentations
 */
@Schema(description = "Payload for creating or modifying sprint review entries")
public record CreateReviewRequestDto(

    @NotBlank @Schema(description = "Associated ticket or issue tracking number", example = "FEAT-2048",
        requiredMode = Schema.RequiredMode.REQUIRED) String ticketNumber,

    @NotBlank @Schema(description = "Title of the review item", example = "Async Event Pipeline Migration",
        requiredMode = Schema.RequiredMode.REQUIRED) String title,

    @Schema(description = "Detailed summary or context of the changes",
        example = "Replaced polling mechanisms with real-time WebSocket event dispatching.") String description,

    @NotNull @Schema(description = "Format category of the review", example = "DEMO",
        requiredMode = Schema.RequiredMode.REQUIRED) ReviewType type,

    @Schema(description = "Markdown notes used during live system demonstrations (for DEMO reviews)",
        example = "1. Open dashboard\n2. Trigger simulated event\n3. Verify notification toast") String demoNotes,

    @ArraySchema(
        schema = @Schema(description = "Bulleted key facts for presentation reviews (for PRESENTATION reviews)",
            example = "Throughput increased by 30%")) List<String> keyFacts) {

    /**
     * Formats and compiles the unified internal content payload depending on {@link ReviewType}.
     *
     * <p>Example usage:
     * <pre>{@code
     * String compiledContent = request.toContentString();
     * }</pre>
     *
     * @return unified plain-text or newline-delimited content string
     */
    public String toContentString() {
        if (type == ReviewType.DEMO) {
            return Objects.requireNonNullElse(demoNotes, "").trim();
        }
        if (type == ReviewType.PRESENTATION && keyFacts != null && !keyFacts.isEmpty()) {
            return keyFacts.stream().filter(Objects::nonNull).map(String::trim).filter(fact -> !fact.isEmpty())
                .collect(Collectors.joining("\n"));
        }
        return "";
    }
}