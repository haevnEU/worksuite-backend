package de.haevn.worksuite.time;



import de.haevn.worksuite.common.RestApiController;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/time-entries")
public class TimeController {
    private final TimeService timeService;

    @GetMapping
    public List<TimeDTO> getTimeEntries(@RequestParam(required = false) final Optional<Integer> history) {
        final List<TimeEntry> modelList;
        if (history.isPresent()) {
            modelList = timeService.getAll(history.get());
        } else {
            modelList = timeService.getForToday();
        }
        return modelList.stream().map(TimeDTO::new).toList();
    }
}
