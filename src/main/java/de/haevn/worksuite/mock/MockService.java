package de.haevn.worksuite.mock;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Slf4j
@Service
public class MockService {

    private final MockGeneratorRegistry mockGeneratorRegistry;

    public Set<MockType> getSupportedMocks() {
        log.info("Getting supported mocks");
        return mockGeneratorRegistry.getRegisteredTypes();
    }

    public String getMockData(final MockType type, final int amount) {
        log.info("Getting mock data for type: {}", type);
        return mockGeneratorRegistry.getMockGenerator(type).createMockData(amount);
    }
}
