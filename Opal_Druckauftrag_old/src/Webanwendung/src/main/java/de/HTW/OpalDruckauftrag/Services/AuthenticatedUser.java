package de.HTW.OpalDruckauftrag.Services;

import de.HTW.OpalDruckauftrag.entities.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Authenticated User erbt von der Klasse User und implementiert das UserDetails Interface
 * @author Francesco Ryplewitz
 * @version 1.2
 */
public class AuthenticatedUser extends User implements UserDetails {
    protected AuthenticatedUser(User user) {
        super(user.getUsername(), user.getPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_USER");
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}