package de.haevn.worksuite.download;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration of supported file download and export targets.
 *
 * <p>Example usage:
 * <pre>{@code
 * DownloadModule module = DownloadModule.WEEKLY_MEETING_PROTOCOL;
 * }</pre>
 */
@Schema(description = "Identifies the module type for download and export operations")
public enum DownloadModule {

    @Schema(description = "Export weekly meeting notes as a PDF document") WEEKLY_MEETING_PROTOCOL,

    @Schema(description = "Export a specific notebook entry as a PDF document") NOTEBOOK_EXPORT,

    @Schema(description = "Export a sprint retrospective summary as a PDF document") RETROSPECTIVE_PROTOCOL,

    @Schema(description = "Stream a remote ticket attachment from Redmine") TICKET_ATTACHMENT,

    @Schema(description = "Download a shared file from the internal workspace share storage") FILE_SHARE
}