package de.haevn.worksuite.download;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * Request payload containing parameters required to resolve, render, or proxy downloadable resources.
 *
 * <p>Example usage:
 * <pre>{@code
 * RequestDTO request = new RequestDTO(
 *     "redmine-api-key-xyz",
 *     "meeting-notes.pdf",
 *     "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
 *     false,
 *     null,
 *     "https://pm.hausheld.info/attachments/download/123/report.csv"
 * );
 * }</pre>
 *
 * @param apiKey optional API key required for remote service authentication (e.g., Redmine)
 * @param filename optional custom filename override for the downloaded resource
 * @param id entity or file identifier (e.g., UUID string)
 * @param isDraft indicator whether documents should be rendered with draft markings
 * @param url general purpose target URL
 * @param webUrl fully qualified remote resource URI for remote attachment downloads
 */
@Schema(description = "Request parameters for resolving or generating downloadable resources")
public record RequestDTO(

    @Schema(description = "API authentication key for remote systems",
        example = "9f8e7d6c5b4a3210fedcba9876543210") String apiKey,

    @Schema(description = "Custom filename override for the downloaded attachment",
        example = "sprint-24-retro.pdf") String filename,

    @Schema(description = "Target entity or file unique identifier (UUID string)",
        example = "c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a") String id,

    @Schema(description = "Whether the document should include draft markings and watermarks", example = "false",
        defaultValue = "false") Boolean isDraft,

    @Schema(description = "Target reference URL", example = "https://worksuite.haevn.de/files/1024") String url,

    @Schema(description = "Direct remote download URI for attachments",
        example = "https://pm.hausheld.info/attachments/download/4567/spec.pdf") String webUrl) {

    /**
     * Resolves the {@code isDraft} flag with a safe non-null fallback to {@code false}.
     *
     * @return {@code true} if marked as draft, otherwise {@code false}
     */
    public boolean draftStatus() {
        return Objects.requireNonNullElse(isDraft, Boolean.FALSE);
    }
}