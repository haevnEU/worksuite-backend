package de.haevn.worksuite.vcs;

import jakarta.validation.constraints.NotBlank;

public record MrProtocolRequest(String description,

                                @NotBlank(message = "Ticket-ID muss angegeben werden") String ticketId,

                                Boolean hasImportantChanges, String importantChanges,

                                Boolean hasTestSetup, Boolean hasUnitTests, String unitTests,

                                Boolean hasManualTests, String manualTests,

                                Boolean hasBreakingChanges, Boolean hasDatabaseSchemaChanges,
                                Boolean hasDatabaseViewsChanges) {
    public MrProtocolRequest {
        hasImportantChanges = Boolean.TRUE.equals(hasImportantChanges);
        hasTestSetup = Boolean.TRUE.equals(hasTestSetup);
        hasUnitTests = Boolean.TRUE.equals(hasUnitTests);
        hasManualTests = Boolean.TRUE.equals(hasManualTests);
        hasBreakingChanges = Boolean.TRUE.equals(hasBreakingChanges);
        hasDatabaseSchemaChanges = Boolean.TRUE.equals(hasDatabaseSchemaChanges);
        hasDatabaseViewsChanges = Boolean.TRUE.equals(hasDatabaseViewsChanges);
    }
}