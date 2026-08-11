package de.haevn.worksuite.weekly;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WeeklyMeetingDTO(
    UUID id,
    String title,
    String summary,
    List<DaySummaryDTO> daySummaries,
    Instant createdAt
) {

    public static WeeklyMeetingDTO fromModel(final WeeklyMeeting weeklyMeeting) {
        if (weeklyMeeting == null) return null;

        final List<DaySummaryDTO> daySummaryDTOs = (weeklyMeeting.getDaySummaries() == null)
            ? List.of()
            : weeklyMeeting.getDaySummaries().stream()
            .map(DaySummaryDTO::fromModel)
            .toList();

        return new WeeklyMeetingDTO(
            weeklyMeeting.getId(),
            weeklyMeeting.getTitle(),
            weeklyMeeting.getSummary(),
            daySummaryDTOs,
            weeklyMeeting.getCreatedAt()
        );
    }

    public WeeklyMeeting toModel() {
        final WeeklyMeeting weeklyMeeting = new WeeklyMeeting();
        weeklyMeeting.setId(this.id());
        weeklyMeeting.setTitle(this.title());
        weeklyMeeting.setSummary(this.summary());
        weeklyMeeting.setCreatedAt(this.createdAt());

        if (this.daySummaries() != null) {
            this.daySummaries().stream()
                .map(DaySummaryDTO::toModel)
                .forEach(weeklyMeeting::addDaySummary);
        }

        return weeklyMeeting;
    }
}