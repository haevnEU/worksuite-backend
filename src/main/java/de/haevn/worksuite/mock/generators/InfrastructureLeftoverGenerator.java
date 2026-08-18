package de.haevn.worksuite.mock.generators;

import de.haevn.worksuite.mock.MockType;
import de.haevn.worksuite.mock.MockUtils;
import org.springframework.stereotype.Component;

@Component
public class InfrastructureLeftoverGenerator implements MockGenerator {


    @Override
    public MockType getType() {
        return MockType.INFRA_LEFTOVER;
    }

    @Override
    public String getHeader() {
        return "Name;Mechaniker-ID;Termin";
    }

    @Override
    public String createRow() {
        final String name = MockUtils.createRandomName();
        final String mechanicId = MockUtils.createRandomValue(5);
        final String appointment = MockUtils.createRandomDateTime();

        final String template = "%s;%s;%s";
        return String.format(template, name, mechanicId, appointment);
    }
}
