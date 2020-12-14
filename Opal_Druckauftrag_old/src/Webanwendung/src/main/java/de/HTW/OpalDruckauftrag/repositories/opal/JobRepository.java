package de.HTW.OpalDruckauftrag.repositories.opal;

import de.HTW.OpalDruckauftrag.entities.opal.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Collection;

/**
 * Implementiert: JPARepository (Sammlung aller SQL-Statements zu Druckjobs)
 * alle Funktionen mit Query annotiert, nativeQuery=true
 * @author Francesco Ryplewitz, Felix Müller
 * @version 1.0
 */

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {


    /**
     * alle Druckjobs mit Druckstatus == 1
     * @return Collection aller gefundenen Druckjobs
     */
    @Query(value = "SELECT * FROM Druckjob j WHERE j.Druckstatus = 1",
            nativeQuery = true)
    Collection<Job> findAllPrinted();

    /**
     * alle Druckjobs mit Druckstatus == 0
     * @return Collection aller gefundenen Druckjobs
     */
    @Query(value = "SELECT * FROM Druckjob j WHERE j.Druckstatus = 0",
            nativeQuery = true)
    Collection<Job> findAllUnprinted();

    /**
     * alle Druckjobs zu einem Auftraggeber (Bibliotheksnummer)
     * @param bibliotheksnummer Suchparameter
     * @return Collection aller gefundenen Druckjobs
     */
    @Query(value = "SELECT * FROM Druckjob j WHERE j.Auftraggeber_Bibliotheksnummer =?1",
            nativeQuery = true)
    Collection<Job>findByID(String bibliotheksnummer);

    /**
     * zusätzlich mit Transactional und Modifying annotiert (DB-Entitys werden verändert)
     * alle Druckjobs mit bestimmter Job_ID löschen
     * @param JodId Bedingung
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Druckjob WHERE Job_ID =?1",
            nativeQuery= true)
    void deleteByID(String JodId);

    /**
     * zusätzlich mit Transactional und Modifying annotiert (DB-Entitys werden verändert)
     * alle Druckjobs mit bestimmter Job_ID updaten (Druckstatus = 1)
     * @param JodId Bedingung
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE Druckjob j SET j.Druckstatus = 1 WHERE j.Job_ID =?1",
            nativeQuery= true)
    void updateByID(String JodId);

    /**
     * Alle zeitkritischen Druckjobs gesperrter Nutzer finden
     * @return Collection aller gefundenen Druckjobs
     */
    @Query(value =  "SELECT * FROM Druckjob j " +
                    "WHERE (DATEDIFF(CURDATE(), j.Auftragsdatum)) >7 " +
                    "AND j.Auftraggeber_Bibliotheksnummer IN (SELECT Bibliotheksnummer FROM Benutzer b WHERE b.Sperrung = 1);",
            nativeQuery = true)
    Collection<Job>findCriticalJobsBlocked();

    /**
     * Alle zeitkritischen Druckjobs gesperrter Nutzer finden
     * @return Collection aller gefundenen Druckjobs
     */
    @Query(value =  "SELECT * FROM Druckjob j " +
            "WHERE (DATEDIFF(CURDATE(), j.Auftragsdatum)) >7 " +
            "AND j.Auftraggeber_Bibliotheksnummer IN (SELECT Bibliotheksnummer FROM Benutzer b WHERE b.Sperrung = 0);",
            nativeQuery = true)
    Collection<Job>findCriticalJobsNotBlocked();

    /**
     * S-Nummer des Auftraggebers eines Druckjobs finden
     * @param JobID des Jobs
     * @return S-Nummer des Auftraggebers
     */
    @Query(
            value = "Select j.Auftraggeber_Bibliotheksnummer FROM Druckjob j WHERE j.Job_ID = ?1 ",
            nativeQuery = true
    )
    String getSnummerByJobID(short JobID);

    /**
     * zusätzlich mit Transactional und Modifying annotiert (DB-Entitys werden verändert)
     * alle Druckjobs mit bestimmter Job_ID updaten (Printed = 1)
     * @param JodId Bedingung
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE Druckjob j SET j.Printed = 1 WHERE j.Job_ID =?1",
            nativeQuery= true)
    void updatePrinted(String JodId);
}