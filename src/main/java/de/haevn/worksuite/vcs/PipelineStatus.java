package de.haevn.worksuite.vcs;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PipelineStatus {
    SUCCESS("success"), FAILED("failed"), RUNNING("running"), CANCELED("canceled"), SKIPPED("skipped"), PENDING("pending");

    private final String value;

    PipelineStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}