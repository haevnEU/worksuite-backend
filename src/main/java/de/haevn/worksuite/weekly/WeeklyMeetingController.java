package de.haevn.worksuite.weekly;

import de.haevn.worksuite.common.RestApiController;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/weekly-meetings")
public class WeeklyMeetingController {

    private final WeeklyMeetingService weeklyMeetingService;


    public record AddTaskRequest(String task) {
    }


    public record UpdateSummaryRequest(String summary) {
    }

    @GetMapping
    public ResponseEntity<List<WeeklyMeetingDTO>> getAll() {
        return ResponseEntity.ok(weeklyMeetingService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WeeklyMeetingDTO> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(weeklyMeetingService.getById(id));
    }

    @PostMapping("/generate")
    public ResponseEntity<Void> generateWeeklyMeeting() {
        weeklyMeetingService.generateNextWeek();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<Void> addTaskToDay(@PathVariable("id") UUID meetingId,
        @RequestParam("day") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
        @RequestBody AddTaskRequest request) {
        weeklyMeetingService.addToMeeting(day, meetingId, request.task());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/day-summary")
    public ResponseEntity<Void> updateDaySummary(@PathVariable("id") UUID meetingId,
        @RequestParam("day") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
        @RequestBody UpdateSummaryRequest request) {
        weeklyMeetingService.addDaySummary(day, meetingId, request.summary());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/summary")
    public ResponseEntity<Void> updateWeeklySummary(@PathVariable("id") UUID meetingId,
        @RequestBody UpdateSummaryRequest request) {
        weeklyMeetingService.addSummary(meetingId, request.summary());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<Resource> exportPdf(@PathVariable("id") UUID meetingId,
        @RequestHeader(value = "isDraft", defaultValue = "false") final boolean isDraft) {
        return weeklyMeetingService.exportPdf(meetingId, isDraft);
    }
}