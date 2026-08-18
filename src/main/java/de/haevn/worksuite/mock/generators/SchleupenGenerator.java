package de.haevn.worksuite.mock.generators;

import de.haevn.worksuite.mock.MockType;
import org.springframework.stereotype.Component;

@Component
public class SchleupenGenerator implements MockGenerator {
    @Override
    public MockType getType() {
        return MockType.SCHLEUPEN;
    }

    @Override
    public String getHeader() {
        return "Header1;Header2";
    }

    @Override
    public String createRow() {
        return "Cell1;Cell2";
    }
}
