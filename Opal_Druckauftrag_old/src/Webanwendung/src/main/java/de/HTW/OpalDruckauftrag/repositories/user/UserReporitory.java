package de.HTW.OpalDruckauftrag.repositories.user;

import de.HTW.OpalDruckauftrag.entities.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
/**
 * Implementiert: CRUDRepository (Sammlung aller SQL-Statements zur Benutzer-Security)
 * alle Funktionen mit Query annotiert, nativeQuery=true
 * @author Francesco Ryplewitz
 * @version 1.2
 */
public interface UserReporitory extends CrudRepository<User,Integer> {

    User findByUsername(String username);

    @Query(value = "Select * From User",
            nativeQuery = true)
    Collection<User>findAllUser();

    @Query(value = "Update User Set password=?1 WHERE id=1",
            nativeQuery = true)
    void ChangePassword(String password);
}
