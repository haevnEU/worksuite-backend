package de.haevn.worksuite.review;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Categorization of sprint review presentations and deliverables.
 *
 * <p>Example usage:
 * <pre>{@code
 * ReviewType type = ReviewType.DEMO;
 * }</pre>
 */
@Schema(description = "Categorization of the review presentation format")
public enum ReviewType {

    @Schema(description = "Live feature demonstration containing demo walkthrough notes") DEMO,

    @Schema(description = "High-level slide or stakeholder presentation containing structured key facts") PRESENTATION
}