package de.haevn.worksuite.mock;

import io.jsonwebtoken.lang.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Slf4j
@Service
public class MockService {

    public List<String> getSupportedMocks() {
        log.info("Getting supported mocks");
        return Collections.of(MockType.values()).stream().map(Enum::name).toList();
    }

    public Map<String, Object> getMockData(final MockType type) {
        log.info("Getting mock data for type: {}", type);
        return Map.of();
    }



}
