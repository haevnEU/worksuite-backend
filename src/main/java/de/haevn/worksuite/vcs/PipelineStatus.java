package de.haevn.worksuite.vcs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration representing the execution state of a CI/CD pipeline in the VCS provider.
 *
 * <p>Example usage:
 * <pre>{@code
 * PipelineStatus status = PipelineStatus.SUCCESS;
 * String rawValue = status.getValue(); // "success"
 * }</pre>
 */
@Schema(description = "VCS CI/CD pipeline execution status")
public enum PipelineStatus {

    @Schema(description = "Pipeline execution finished successfully") SUCCESS("success"),

    @Schema(description = "Pipeline execution failed") FAILED("failed"),

    @Schema(description = "Pipeline is currently executing") RUNNING("running"),

    @Schema(description = "Pipeline execution was manually canceled") CANCELED("canceled"),

    @Schema(description = "Pipeline execution was skipped") SKIPPED("skipped"),

    @Schema(description = "Pipeline is queued and pending execution") PENDING("pending");

    private final String value;

    PipelineStatus(final String value) {
        this.value = value;
    }

    /**
     * Resolves a {@link PipelineStatus} enum constant from a string representation.
     *
     * <p>Example usage:
     * <pre>{@code
     * PipelineStatus status = PipelineStatus.fromValue("success");
     * }</pre>
     *
     * @param value the raw text value to resolve
     * @return the matched {@link PipelineStatus} or {@link #PENDING} as fallback
     */
    @JsonCreator
    public static PipelineStatus fromValue(final String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        for (final PipelineStatus status : values()) {
            if (status.value.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return PENDING;
    }

    /**
     * Returns the raw serialized string value corresponding to the GitLab pipeline status.
     *
     * @return lowercase status value string
     */
    @JsonValue
    public String getValue() {
        return value;
    }
}