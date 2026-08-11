package de.haevn.worksuite.gitlab;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PipelineStatus {
    SUCCESS("success"),
    FAILED("failed"),
    RUNNING("running"),
    CANCELED("canceled"),
    SKIPPED("skipped");

    private final String value;

    PipelineStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}