package de.haevn.worksuite.push.events;

import java.time.Instant;

public record WsEvent(String source, Priority priority, String payload, Instant timestamp) {
    public WsEvent(Class<?> source, Priority priority, String payload) {
        this(source.getSimpleName(), priority, payload, Instant.now());
    }
}
