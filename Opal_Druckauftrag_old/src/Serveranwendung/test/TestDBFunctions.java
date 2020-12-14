import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDBFunctions {

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
	public void testBenutzerExists() {
		
		Benutzer benutzer = new Benutzer();
		
		assertTrue(benutzer.exists());
		
	}
	
	@Test
	public void testDBFunctions() {
		
		Benutzer benutzer = new Benutzer("s11111", false, "Max", "Muusstermann");
		benutzer.insert();
		benutzer.Nachname = "Mustermann";
		assertEquals(0, benutzer.update());
		
		Druckjob druckjob = new Druckjob("s11111", 1, 1.0, false, "2020-10-10");
		druckjob.insert();
		druckjob.setID();	
		assertEquals(0, druckjob.delete());
	
		
		Druckauftrag druckauftrag = new Druckauftrag(100, "s11111", false, 1, 1, 12, "File.pdf", 1);
		assertEquals(0, druckauftrag.insert());
		druckauftrag.setID();
		
		druckauftrag.deleteElement();
		benutzer.delete();
		
	}
}
