package de.haevn.worksuite.mock.generators;

import de.haevn.worksuite.mock.MockType;
import de.haevn.worksuite.mock.MockUtils;
import org.springframework.stereotype.Component;

@Component
public class CustomerExchangeGenerator implements MockGenerator {
    @Override
    public MockType getType() {
        return MockType.CUSTOMER_EXCHANGE;
    }

    @Override
    public String getHeader() {
        return "Seriennummer;Installationsdatum;Messlokations-ID (MELO);MALO;Objekt-Adresse;Objekt-PLZ;Objekt-Ort;Stadtteil/Bezirk/Areal;Registerangaben;Register- / Zählerstände Einbauzähler zum Einbauzeitpunkt";
    }

    @Override
    public String createRow() {
        final String serialNumber = MockUtils.createRandomValue(10);
        final String installationDate = MockUtils.createRandomDate();
        final String meloId = MockUtils.createRandomValue(8);
        final String malo = MockUtils.createRandomValue(8);
        final String objectAddress =
            MockUtils.Address.STREET + " " + MockUtils.Address.HOUSE_NUMBER + MockUtils.Address.HOUSE_NUMBER_ADDITION;
        final String objectZipCode = MockUtils.Address.ZIP_CODE;
        final String objectCity = MockUtils.Address.CITY;
        final String district = MockUtils.createRandomValue(5);
        final String registerInfo = MockUtils.createRandomValue(15);
        final String registerCounter = MockUtils.createRandomValue(10);

        final String template = "%s;%s;%s;%s;%s;%s;%s;%s;%s;%s";
        return String.format(template, serialNumber, installationDate, meloId, malo, objectAddress, objectZipCode,
            objectCity, district, registerInfo, registerCounter);
    }
}
