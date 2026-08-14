package de.haevn.worksuite.common;

import java.util.UUID;

public record UserIntegrationContext(
    UUID userId,
    String redmineApiKey,
    String vcsToken
) {}