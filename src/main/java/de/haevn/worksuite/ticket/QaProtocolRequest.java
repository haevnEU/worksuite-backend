package de.haevn.worksuite.ticket;

import jakarta.validation.constraints.NotNull;

public record QaProtocolRequest(
    @NotNull(message = "Pipeline-Status muss angegeben werden")
    Boolean pipelineSuccess,

    String pipelineFailReason,

    @NotNull(message = "Rebase-Status muss angegeben werden")
    Boolean rebaseExecuted,

    String intro,

    Boolean hasAcceptanceCriteria,
    String acceptanceCriteria,

    Boolean hasTestSetup,
    String testSetup,

    Boolean hasUnitTests,
    String unitTests,

    Boolean hasTestDatasets,
    String testDatasets,

    Boolean hasSideEffects,
    String sideEffects,

    Boolean hasChangedEndpoints,
    String changedEndpoints
) {
    public QaProtocolRequest {
        hasAcceptanceCriteria = Boolean.TRUE.equals(hasAcceptanceCriteria);
        hasTestSetup = Boolean.TRUE.equals(hasTestSetup);
        hasUnitTests = Boolean.TRUE.equals(hasUnitTests);
        hasTestDatasets = Boolean.TRUE.equals(hasTestDatasets);
        hasSideEffects = Boolean.TRUE.equals(hasSideEffects);
        hasChangedEndpoints = Boolean.TRUE.equals(hasChangedEndpoints);

        if (Boolean.FALSE.equals(pipelineSuccess) && (pipelineFailReason == null || pipelineFailReason.isBlank())) {
            throw new IllegalArgumentException("Ein Grund für den Pipeline-Fehler muss angegeben werden.");
        }
    }
}