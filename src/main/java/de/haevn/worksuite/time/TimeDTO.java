package de.haevn.worksuite.time;

import java.time.Instant;
import java.util.UUID;

public record TimeDTO(UUID id, int hours, int minutes, Instant date, String description, Instant createdAt,
                      long activityId, long ticketId) {
    public TimeDTO(final TimeModel timeModel) {
        this(timeModel.getId(), timeModel.getHours(), timeModel.getMinutes(), timeModel.getDate(),
            timeModel.getDescription(), timeModel.getCreatedAt(), timeModel.getActivityId(), timeModel.getTicketId());
    }
}
