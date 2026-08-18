package de.haevn.worksuite.mock.generators;

import de.haevn.worksuite.mock.MockType;
import de.haevn.worksuite.mock.MockUtils;
import org.springframework.stereotype.Component;

@Component
public class SMLeftoverGenerator implements MockGenerator {
    @Override
    public MockType getType() {
        return MockType.SM_LEFTOVER;
    }

    @Override
    public String getHeader() {
        return "MeLo;Altzählernummer;Mechaniker-ID;Termin";
    }

    @Override
    public String createRow() {
        final String melo = MockUtils.createMelo();
        final String altzaehlernummer = MockUtils.nextMeterNumber();
        final String mechanicId = MockUtils.createRandomValue(5);
        final String appointment = MockUtils.createRandomDateTime();
        final String template = "%s;%s;%s;%s";
        return String.format(template, melo, altzaehlernummer, mechanicId, appointment);
    }
}
