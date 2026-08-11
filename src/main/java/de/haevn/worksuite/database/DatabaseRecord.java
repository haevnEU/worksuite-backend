package de.haevn.worksuite.database;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Immutable data transfer object representing a generic database table entry.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * DatabaseRecord record = new DatabaseRecord(
 *     101L,
 *     "STATUS_ACTIVE",
 *     "Datensatz ist aktiv und verfügbar.",
 *     "Record is active and available."
 * );
 * }</pre>
 *
 * @param id the unique numerical identifier of the record
 * @param key the unique business key or code string
 * @param descriptionGer localized German description text
 * @param descriptionEng localized English description text
 */
@Schema(description = "Represents a single database table record entry")
public record DatabaseRecord(

    @Schema(description = "Numerical primary identifier", example = "1001") long id,

    @Schema(description = "Unique alphanumeric business key", example = "USR_ROLE_ADMIN") String key,

    @Schema(description = "German description text",
        example = "Administrator-Rolle mit vollen Rechten") String descriptionGer,

    @Schema(description = "English description text",
        example = "Administrator role with full system privileges") String descriptionEng) {
}