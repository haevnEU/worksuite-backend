package de.haevn.worksuite.mock.generators;

import de.haevn.worksuite.mock.MockType;
import de.haevn.worksuite.mock.MockUtils;
import org.springframework.stereotype.Component;

@Component
public class MasterdataGenerator implements MockGenerator {
    private static final String ROW_TEMPLATE =
        "%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s";

    @Override
    public MockType getType() {
        return MockType.MASTERDATA;
    }

    @Override
    public String getHeader() {
        return "Messlokations-ID;Zählernummer;(GLN) Netzbetreiber;Projektname;(GLN) Messstellenbetreiber;Lieferant;Turnus Lieferant;Gerätetyp SOLL;Anbringungsart;Wandlermessung;Wandlerfaktor;geeicht bis;Sondergeräte;Sparte;Tarif-ID;Jahresverbrauch Durchschnitt letzte 3 Jahre (kWh);Jahresverbrauch t-1 (kWh);Jahresverbrauch t-2 (kWh);Jahresverbrauch t-3 (kWh);Datum letzte Ablesung;Zählerstand letzte Ablesung;Pflichteinbau;Installationsadresse Vorname;Installationsadresse Name;Installationsadresse Straße;Installationsadresse Hausnummer;Installationsadresse Hausnummernzusatz;Installationsadresse PLZ;Installationsadresse Ort;Anrede Anschreiben;Vorname Anschreiben;Nachname Anschreiben;Firmenname Anschreiben;Straße Anschreiben;Hausnummer Anschreiben;Hausnummernzusatz Anschreiben;Adresszusatz Anschreiben;PLZ Anschreiben;Ort Anschreiben;Stadtteil/Bezirk/Areal;Telefonnummer Anschreiben;E-Mail Anschreiben;Zusatzinfo Zählerstandort;Gebäudenutzung;Kundenreferenz;Zusatzreferenz;Vorname Anschlussnehmer;Nachname Anschlussnehmer;Firmenname Anschlussnehmer;Straße Anschlussnehmer;Hausnummer Anschlussnehmer;Hausnummernzusatz Anschlussnehmer;Adresszusatz Anschlussnehmer;PLZ Anschlussnehmer;Ort Anschlussnehmer;Telefonnummer Anschlussnehmer;E-Mail Anschlussnehmer;Hinweisfeld";
    }


    @Override
    public String createRow() {
        // 1-10
        final String meloId = MockUtils.createMelo();
        final String zaehlernummer = MockUtils.nextMeterNumber();
        final String glnNetzbetreiber = MockUtils.getGLN();
        final String projektname = "";
        final String glnMessstellenbetreiber = glnNetzbetreiber;
        final String lieferant = MockUtils.createRandomWord(10);
        final String turnusLieferant = "";
        final String geraetetypSoll = MockUtils.getDeviceType();
        final String anbringungsart = MockUtils.getAffixation();
        final String wandlermessung = MockUtils.getConversion();

        // 11-20
        final String wandlerfaktor =
            wandlermessung.toLowerCase().contains("nieder") ? MockUtils.createRandomValue(1000) : "1";
        final String geeichtBis = MockUtils.getYearFromThis(4);
        final String sondergeraete = "keine";
        final String sparte = "Stromg";
        final String tarifId = "7";
        final String jahresverbrauchDurchschnitt3Jahre = MockUtils.createRandomValue(10000);
        final String jahresverbrauchTMinus1 = MockUtils.createRandomValue(10000);
        final String jahresverbrauchTMinus2 = MockUtils.createRandomValue(10000);
        final String jahresverbrauchTMinus3 = MockUtils.createRandomValue(10000);
        final String datumLetzteAblesung = MockUtils.createRandomDate();

        // 21-30
        final String zaehlerstandLetzteAblesung = MockUtils.createRandomValue(10000);
        final String pflichteinbau = "nein";
        final String installAdresseVorname = MockUtils.createRandomName();
        final String installAdresseName = MockUtils.createRandomName();
        final String installAdresseStrasse = "";
        final String installAdresseHausnummer = MockUtils.Address.HOUSE_NUMBER;
        final String installAdresseHausnummernzusatz = MockUtils.Address.HOUSE_NUMBER_ADDITION;
        final String installAdressePlz = MockUtils.Address.ZIP_CODE;
        final String installAdresseOrt = MockUtils.Address.CITY;
        final String anredeAnschreiben = "";

        // 31-40
        final String vornameAnschreiben = MockUtils.createRandomName();
        final String nachnameAnschreiben = MockUtils.createRandomName();
        final String firmennameAnschreiben = "";
        final String strasseAnschreiben = MockUtils.Address.STREET;
        final String hausnummerAnschreiben = MockUtils.Address.HOUSE_NUMBER;
        final String hausnummernzusatzAnschreiben = MockUtils.Address.HOUSE_NUMBER_ADDITION;
        final String adresszusatzAnschreiben = "";
        final String plzAnschreiben = MockUtils.Address.ZIP_CODE;
        final String ortAnschreiben = MockUtils.Address.CITY;
        final String stadtteilBezirkAreal = "";

        // 41-50
        final String telefonnummerAnschreiben = "";
        final String emailAnschreiben = "";
        final String zusatzinfoZaehlerstandort = "";
        final String gebaeudenutzung = "";
        final String kundenreferenz = "";
        final String zusatzreferenz = "";
        final String vornameAnschlussnehmer = MockUtils.createRandomName();
        final String nachnameAnschlussnehmer = MockUtils.createRandomName();
        final String firmennameAnschlussnehmer = "";
        final String strasseAnschlussnehmer = MockUtils.Address.STREET;

        // 51-58
        final String hausnummerAnschlussnehmer = MockUtils.Address.HOUSE_NUMBER;
        final String hausnummernzusatzAnschlussnehmer = MockUtils.Address.HOUSE_NUMBER_ADDITION;
        final String adresszusatzAnschlussnehmer = "";
        final String plzAnschlussnehmer = MockUtils.Address.ZIP_CODE;
        final String ortAnschlussnehmer = MockUtils.Address.CITY;
        final String telefonnummerAnschlussnehmer = "";
        final String emailAnschlussnehmer = "";
        final String hinweisfeld = "";

        return String.format(ROW_TEMPLATE, meloId, zaehlernummer, glnNetzbetreiber, projektname,
            glnMessstellenbetreiber, lieferant, turnusLieferant, geraetetypSoll, anbringungsart, wandlermessung,
            wandlerfaktor, geeichtBis, sondergeraete, sparte, tarifId, jahresverbrauchDurchschnitt3Jahre,
            jahresverbrauchTMinus1, jahresverbrauchTMinus2, jahresverbrauchTMinus3, datumLetzteAblesung,
            zaehlerstandLetzteAblesung, pflichteinbau, installAdresseVorname, installAdresseName, installAdresseStrasse,
            installAdresseHausnummer, installAdresseHausnummernzusatz, installAdressePlz, installAdresseOrt,
            anredeAnschreiben, vornameAnschreiben, nachnameAnschreiben, firmennameAnschreiben, strasseAnschreiben,
            hausnummerAnschreiben, hausnummernzusatzAnschreiben, adresszusatzAnschreiben, plzAnschreiben,
            ortAnschreiben, stadtteilBezirkAreal, telefonnummerAnschreiben, emailAnschreiben, zusatzinfoZaehlerstandort,
            gebaeudenutzung, kundenreferenz, zusatzreferenz, vornameAnschlussnehmer, nachnameAnschlussnehmer,
            firmennameAnschlussnehmer, strasseAnschlussnehmer, hausnummerAnschlussnehmer,
            hausnummernzusatzAnschlussnehmer, adresszusatzAnschlussnehmer, plzAnschlussnehmer, ortAnschlussnehmer,
            telefonnummerAnschlussnehmer, emailAnschlussnehmer, hinweisfeld);
    }

}
