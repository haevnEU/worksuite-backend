package de.haevn.worksuite.ticket.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for logging hours and minutes against a specific ticket.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * LogTimeRequest request = new LogTimeRequest(
 *     "2026-08-17",
 *     2,
 *     30,
 *     9L,
 *     "Implemented REST endpoints and integration tests."
 * );
 * }</pre>
 *
 * @param day date string in ISO format (YYYY-MM-DD)
 * @param hours spent hours (0-24)
 * @param minutes spent minutes (0-59)
 * @param activityId numerical Redmine activity category ID (e.g. Development, Design)
 * @param comment description of work performed
 */
@Schema(description = "Payload for logging time spent on a ticket")
public record LogTimeRequest(

    @NotBlank(message = "Day is required") @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
        message = "Day must be in ISO format (YYYY-MM-DD)") @Schema(
        description = "Target date for the logged work (YYYY-MM-DD)", example = "2026-08-17",
        requiredMode = Schema.RequiredMode.REQUIRED) String day,

    @Min(value = 0, message = "Hours cannot be negative") @Max(value = 24, message = "Hours cannot exceed 24") @Schema(
        description = "Spent hours", example = "3", minimum = "0", maximum = "24",
        requiredMode = Schema.RequiredMode.REQUIRED) int hours,

    @Min(value = 0, message = "Minutes cannot be negative") @Max(value = 59,
        message = "Minutes cannot exceed 59") @Schema(description = "Spent minutes", example = "45", minimum = "0",
        maximum = "59", requiredMode = Schema.RequiredMode.REQUIRED) int minutes,

    @Schema(description = "Redmine activity identifier", example = "9",
        requiredMode = Schema.RequiredMode.REQUIRED) long activityId,

    @Schema(description = "Description or comment summarizing the logged effort",
        example = "Resolved memory leak in batch processing service") String comment) {
}