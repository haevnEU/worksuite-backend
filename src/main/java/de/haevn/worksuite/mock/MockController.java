package de.haevn.worksuite.mock;

import de.haevn.worksuite.common.RestApiController;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RestApiController("/api/v1/mock")
public class MockController {
    private final MockService mockService;

    @GetMapping
    public Set<MockType> getSupportedMocks() {
        return mockService.getSupportedMocks();
    }

    @GetMapping("/{type}")
    public MockResposneDTO getMockData(@PathVariable final MockType type,
        @RequestParam(defaultValue = "1", required = false) final int amount) {
        final String mock = mockService.getMockData(type, amount);
        return new MockResposneDTO(mock, amount, Instant.now());
    }
}
