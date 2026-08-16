package de.haevn.worksuite.time;

import java.time.Instant;
import java.util.UUID;

public record TimeDTO(UUID id, int hours, int minutes, Instant date, String description, Instant createdAt,
                      long activityId, long ticketId) {
    public TimeDTO(final TimeEntry timeEntry) {
        this(timeEntry.getId(), timeEntry.getHours(), timeEntry.getMinutes(), timeEntry.getDate(),
            timeEntry.getDescription(), timeEntry.getCreatedAt(), timeEntry.getActivityId(), timeEntry.getTicketId());
    }
}
