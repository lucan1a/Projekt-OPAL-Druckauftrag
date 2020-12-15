import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MailTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testReplace() {
		String in = "UTF-8'Dies%20ist%20unser%20Test-String%20mit%20Umlauten%20" + 
				"%C3%A4%20%C3%B6%20%C3%BC%20%C3%9F'";
		String out = "Dies_ist_unser_Test-String_mit_Umlauten_ae_oe_ue_ss";
		
		MailClient mc = new MailClient();
		
		assertEquals(out, mc.replaceName(in));
	}
	
	@Test
	public void testParsers() {
		String in = "OPAL-Druckauftrag\r\n" + 
				"\r\n" + 
				"Alle durch Sie bis Mitternacht erteilten Druckauftr�ge werden" + 
				"gesammelt und im Format A4 - in Graustufen gewandelt, doppelseitig, " +
				"an der langen Seite links zweifach gelocht - ausgedruckt." +
				"Ihr Auftrag ist am n�chsten Werktag ab 10:00 Uhr im Copyshop UNIdruckerei," +
				"Reichenbachstra�e 19, 01069 Dresden (neben der Mensa) abholbereit." +
				"Bitte halten Sie daf�r Ihren Studierendenausweis (Chipkarte) mit Ihrer " +
				"Bibliotheksnummer (s-Nummer) bereit.Kosten�bernahme:Die Kosten " +
				"belaufen sich auf 5 ct pro Druckseite und sind in Summe durch Sie entsprechend in bar" +
				"bzw. ab 10,00 Euro mittels Geldkarte zu begleichen.Datenschutz:" +
				"Ihre Druckdatei(en) und Ihre Bibliotheksnummer werden ausschlie�lich " +
				"f�r den Zweck des Ausdrucks an die UNIdruckerei weiter gegeben." +
				"Mit der Ausl�sung eines Druckauftrages (Button: \"Druckauftrag abschicken\") " +
				"geben Sie Ihre ausdr�ckliche Zustimmung zur Kosten�bernahme und zum Datenschutz." +
				"Bitte beachten Sie folgende Hinweise:Viele Dozentinnen und Dozenten stellen ihre Unterlagen" +
				"erst unmittelbar nach Start der Vorlesung bereit.Der Ausdruck passwortgesch�tzter" +
				"PDF-Dokumente ist technisch bedingt nicht m�glich.Druckauftr�ge, deren Dateigr��e 20 MByte " +
				"�bersteigt k�nnen gegenw�rtig nicht verarbeitet werden.Bei Fragen zum \"OPAL-Druckauftrag\" wenden" +
				"Sie sich bitte an support-ecampus@htw-dresden.de.\r\n" + 
				"\r\n" + 
				"--\r\n" + 
				"Diese Nachricht wurde �ber die Lernplattform OPAL von s12345@htw-dresden.de versandt.\r\n" + 
				"F�r diesen Druckauftrag wurde das Seitenlayout: 2 Seiten pro Blatt gew�hlt.";
		
		MailClient mc = new MailClient();
		String snummer = "s12345";
		String einstellungen = "12";
		
		assertEquals(snummer, mc.getSnr(in));
		assertEquals(einstellungen, mc.getEinstellungen(in));
	}

}
