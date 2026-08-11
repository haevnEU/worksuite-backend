package de.haevn.worksuite.mock;

import de.haevn.worksuite.mock.generators.MasterdataGenerator;
import de.haevn.worksuite.mock.generators.MockGenerator;

public enum MockType {
    MASTER_DATA(new MasterdataGenerator());

    public final MockGenerator mockGenerator;

    MockType(final MockGenerator mockGenerator) {
        this.mockGenerator = mockGenerator;
    }
}
