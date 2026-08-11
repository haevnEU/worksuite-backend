package de.haevn.worksuite.mock.generators;

import de.haevn.worksuite.mock.MockType;

public interface MockGenerator {
    MockType getType();

    String getHeader();

    String createRow();

    default String createMockData(final int amount) {
        final StringBuilder sb = new StringBuilder();
        sb.append(getHeader()).append("\n");
        for (int i = 0; i < amount; i++) {
            sb.append(createRow()).append("\n");
        }
        return sb.toString();
    }
}
