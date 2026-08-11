package de.haevn.worksuite.ticket;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TimeEntryResponse(Long id, Long ticketId, String userName, LocalDate day, int hours, int minutes,
                                String activityId, String comment, OffsetDateTime createdOn) {
}