package de.haevn.worksuite.mock;

import de.haevn.worksuite.mock.generators.MockGenerator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockGeneratorRegistry {
    private final Map<MockType, MockGenerator> mockGenerators = new EnumMap<MockType, MockGenerator>(MockType.class);

    public MockGeneratorRegistry(final List<MockGenerator> mockGenerators) {
        for (final MockGenerator mockGenerator : mockGenerators) {
            final MockType type = mockGenerator.getType();
            if (this.mockGenerators.containsKey(type)) {
                throw new IllegalStateException("Duplicate mock generator for type: " + type);
            }
            this.mockGenerators.put(type, mockGenerator);
            log.info("Registered mock generator for type: {}", type);
        }
    }

    public MockGenerator getMockGenerator(final MockType type) {
        final MockGenerator generator = mockGenerators.get(type);
        if (generator == null) {
            throw new IllegalArgumentException("No mock generator found for type: " + type);
        }
        return generator;
    }

    public Set<MockType> getRegisteredTypes() {
        return mockGenerators.keySet();
    }
}
