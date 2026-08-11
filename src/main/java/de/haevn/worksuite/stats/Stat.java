package de.haevn.worksuite.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Metric categories tracked for daily developer workflow statistics.
 *
 * <p>Example usage:
 * <pre>{@code
 * Stat metric = Stat.MOVED_TO_QA;
 * }</pre>
 */
@Schema(description = "Categories of tracked developer activity statistics")
public enum Stat {

    @Schema(description = "Counter of tickets or tasks moved to Quality Assurance") MOVED_TO_QA,

    @Schema(description = "Counter of tickets rejected or returned from Quality Assurance") RETURN_FROM_QA,

    @Schema(description = "Counter of tickets or Merge Requests moved to code review") MOVED_TO_REVIEW,

    @Schema(description = "Counter of Merge Requests requiring rework from code review") RETURN_FROM_REVIEW
}