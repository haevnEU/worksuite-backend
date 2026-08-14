package de.haevn.worksuite.mock;

import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.common.RestApiController;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@RestApiController("/api/v1/mock")
public class MockController {
    private final MockService mockService;

    @GetMapping
    public List<String> getSupportedMocks(){
        return mockService.getSupportedMocks();
    }

    @PostMapping("/{type}")
    public Map<String, Object> getMockData(@PathVariable final MockType type) throws RedmineException {
        return mockService.getMockData(type);
    }
}
