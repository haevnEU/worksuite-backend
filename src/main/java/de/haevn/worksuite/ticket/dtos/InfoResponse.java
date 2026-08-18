package de.haevn.worksuite.ticket.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data transfer object representing a metadata enumeration entry (such as activity categories or enumeration types).
 *
 * @param id the unique numerical identifier of the metadata entry
 * @param name the human-readable display name of the entry
 * @param isDefault indicates whether this entry is the system default choice
 */
@Schema(description = "Metadata enumeration entry (e.g. time entry activity)")
public record InfoResponse(
    @Schema(description = "Metadata item ID", example = "9")
    long id,

    @Schema(description = "Name of the metadata entry", example = "Development")
    String name,

    @Schema(description = "Whether this is the default activity", example = "true")
    boolean isDefault
) {}