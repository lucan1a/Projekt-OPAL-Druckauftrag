package de.HTW.OpalDruckauftrag.Services;


import de.HTW.OpalDruckauftrag.entities.user.User;
import de.HTW.OpalDruckauftrag.repositories.user.UserReporitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service, stellt Nutzerdaten bereit
 * @author Francesco Ryplewitz
 * @version 1.2
 */
@Service
public class AuthenticatedUserService implements UserDetailsService {

    @Autowired
    private UserReporitory userRepository;


    /**
     * Funktion sucht User aus der WebSecurity/User Datenbanktabelle
     * @param username nach dem gesucht werden soll
     * @return falls der Nutzer vorhanden ist wird ein AuthenticatedUser-Objekt zurückgegeben
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("The user " + username + " does not exist");
        }
        return new AuthenticatedUser(user);
    }
}
