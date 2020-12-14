package de.HTW.OpalDruckauftrag;

import de.HTW.OpalDruckauftrag.entities.opal.Job;
import de.HTW.OpalDruckauftrag.repositories.opal.JobRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.math.BigDecimal;
import java.sql.Date;

@RunWith(JUnit4.class)
@DataJpaTest
public class JobTest {

	@Autowired
	private JobRepository jobRepository;

	private TestEntityManager entityManager;

	@Test
	public void whenFindByName_thenReturnEmployee() {


		short job_ID = 1;
		String auftraggeber_Bibliotheksnummer = "s99999";
		short seitenzahl = 30;
		BigDecimal preis = new BigDecimal(1.5);
		byte druckstatus = 0;
		Date auftragsdatum = new Date(2020,7,31);
		byte printed = 0;
		Job test = new Job(job_ID, auftraggeber_Bibliotheksnummer, seitenzahl, preis, druckstatus, auftragsdatum, printed);
		entityManager.persist(test);
		entityManager.flush();

		jobRepository.deleteByID("1");

		Job found = entityManager.find(Job.class, 1);
		Assert.assertNull(found);
	}
}
