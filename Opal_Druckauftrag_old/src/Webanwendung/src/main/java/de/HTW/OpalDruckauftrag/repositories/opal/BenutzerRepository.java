package de.HTW.OpalDruckauftrag.repositories.opal;

import de.HTW.OpalDruckauftrag.entities.opal.Benutzer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Collection;


/**
 * Implementiert: JPARepository (Sammlung aller SQL-Statements zu Benutzern)
 * alle Funktionen mit Query annotiert, nativeQuery=true
 * @author Francesco Ryplewitz
 * @version 1.0
 */
@Repository
public interface BenutzerRepository extends JpaRepository<Benutzer,Integer> {

    /**
     * alle gesperrten Benutzer
     * @return Collection aller gefundenen Benutzer
     */
    @Query(value = "SELECT * FROM Benutzer j WHERE j.Sperrung = 1 ",
            nativeQuery = true)
    Collection<Benutzer> findBlockedUser();

    /**
     * alle gesperrten Benutzer mit bestimmter Bibliotheksnummer
     * @param bibliotheksnummer Suchparameter
     * @return Collection aller gefundenen Benutzer
     */
    @Query(value = "SELECT * FROM Benutzer j WHERE j.Sperrung = 1 AND j.Bibliotheksnummer =?1",
            nativeQuery = true)
    Collection<Benutzer>findBlockedUserByID(String bibliotheksnummer);

    /**
     * zusätzlich mit Transactional und Modifying annotiert (DB-Entitys werden verändert)
     * Benutzer mit bestimmter SNummer entsperren
     * @param bibliotheksnummer Suchparameter
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE Benutzer j SET j.Sperrung = 0 WHERE j.Bibliotheksnummer  =?1",
            nativeQuery = true)
    void unblockBenutzer(String bibliotheksnummer);

    /**
     * zusätzlich mit Transactional und Modifying annotiert (DB-Entitys werden verändert)
     * Benutzer mit bestimmter SNummer sperren
     * @param bibliotheksnummer Suchparameter
     */
    @Transactional
    @Modifying
    @Query(value = "Update Benutzer j SET j.Sperrung = 1 WHERE j.Bibliotheksnummer  =?1",
            nativeQuery = true)
    void blockBenutzer(String bibliotheksnummer);


    /**
     * Löschen von Benutzern, wenn keine Druckaufträge oder Druckjobs zu diesem Benutzer vorhanden sind
     * @param bibliotheksnummer des zu löschenden Benutzers
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Benutzer WHERE Bibliotheksnummer = ?1  AND ?1 NOT IN (Select Auftraggeber_Bibliotheksnummer FROM Druckauftrag j) AND ?1 NOT IN (Select Auftraggeber_Bibliotheksnummer FROM Druckjob k) AND Sperrung = 0",
    nativeQuery = true)
    void deleteBenutzerByID(String bibliotheksnummer );


}
