package de.haevn.worksuite.mock.generators;

import de.haevn.worksuite.mock.MockUtils;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MasterdataGenerator implements MockGenerator{
    private static final String ROW_TEMPLATE = "%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s";

    @Override
    public String createMockData(final int amount) {
        final List<String> builder = new ArrayList<>();
        builder.add("Messlokations-ID;Zählernummer;(GLN) Netzbetreiber;Projektname;(GLN) Messstellenbetreiber;Lieferant;Turnus Lieferant;Gerätetyp SOLL;Anbringungsart;Wandlermessung;Wandlerfaktor;geeicht bis;Sondergeräte;Sparte;Tarif-ID;Jahresverbrauch Durchschnitt letzte 3 Jahre (kWh);Jahresverbrauch t-1 (kWh);Jahresverbrauch t-2 (kWh);Jahresverbrauch t-3 (kWh);Datum letzte Ablesung;Zählerstand letzte Ablesung;Pflichteinbau;Installationsadresse Vorname;Installationsadresse Name;Installationsadresse Straße;Installationsadresse Hausnummer;Installationsadresse Hausnummernzusatz;Installationsadresse PLZ;Installationsadresse Ort;Anrede Anschreiben;Vorname Anschreiben;Nachname Anschreiben;Firmenname Anschreiben;Straße Anschreiben;Hausnummer Anschreiben;Hausnummernzusatz Anschreiben;Adresszusatz Anschreiben;PLZ Anschreiben;Ort Anschreiben;Stadtteil/Bezirk/Areal;Telefonnummer Anschreiben;E-Mail Anschreiben;Zusatzinfo Zählerstandort;Gebäudenutzung;Kundenreferenz;Zusatzreferenz;Vorname Anschlussnehmer;Nachname Anschlussnehmer;Firmenname Anschlussnehmer;Straße Anschlussnehmer;Hausnummer Anschlussnehmer;Hausnummernzusatz Anschlussnehmer;Adresszusatz Anschlussnehmer;PLZ Anschlussnehmer;Ort Anschlussnehmer;Telefonnummer Anschlussnehmer;E-Mail Anschlussnehmer;Hinweisfeld");
        for(int i = 0; i < amount; i++){
            builder.add(createRow());
        }
        return String.join("\n", builder);
    }

    private String createRow() {
        final String melo = MockUtils.createMelo();
        final String serial = MockUtils.nextMeterNumber();
        final String gln = "9907287000004";
        final String project = "";
        final String gln2 = "9904431000004";
        final String supplier = "Lieferant";
        final String turnus = "";
        final String deviceType = "1-Richtung, Eintarif - Bezug";
        final String mountingType = "Dreipunkt";
        final String transformerMeasurement = "Direktmessung";
        final String transformerFactor = "1";
        final String calibratedUntil = MockUtils.getYearFromThis(0) ;
        final String specialDevices = "";
        final String branch = "Strom";
        final String tariffId = "7";
        final String averageConsumption = MockUtils.createRandomValue();
        final String consumptionT1 = MockUtils.createRandomValue();
        final String consumptionT2 = MockUtils.createRandomValue();
        final String consumptionT3 = MockUtils.createRandomValue();
        final String lastReadingDate = MockUtils.getYearFromThis(-1);
        final String lastReadingValue = MockUtils.createRandomValue();
        final String mandatoryInstallation = "Ja";
        final String installationFirstName = MockUtils.createRandomName();
        return String.format(ROW_TEMPLATE, melo);
    }

}
