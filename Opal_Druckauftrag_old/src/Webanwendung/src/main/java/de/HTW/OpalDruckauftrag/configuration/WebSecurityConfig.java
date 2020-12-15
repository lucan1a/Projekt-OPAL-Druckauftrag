package de.HTW.OpalDruckauftrag.configuration;

import de.HTW.OpalDruckauftrag.Services.AuthenticatedUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Grundlegende Configuration der WebSecurity.
 * Die Klasse "WebSecurityConfig" wird bei jeder Request geladen
 * Spring übernimmt in der SecurityConfiguration einen Großteil der Arbet, d.H. viele Methodenaufrufe geschehen im Hintergrund.
 * Um die Funktionsweise dieser SecurityConfiguration vollständig zu überblicken, empfiehlt sich die Spring Security Dokumentation
 */
@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = AuthenticatedUserService.class)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * bindet den UserDetailService ein
     */
    @Autowired
    private UserDetailsService userDetailsService;


    /**
     * Konfiguriert den Zugriffsschutz der Seite
     * Alle Requests nach "/style.css" sind ohne Authentisierung möglich
     * Alle anderen Requests erfordern eine Authentisierung
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/style.css").permitAll()
                .anyRequest().authenticated()
                .and().formLogin().loginPage("/login").permitAll()
                .and().logout().permitAll();
    }

    /**
     * Lädt mit dem AuthentificationManagerBuilder den UserDetailService, um User zu authentifizieren
     * @param auth AuthentificationManagerBuilder
     */
    @Autowired
    public void globalSecurityConfiguration(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    /**
     * Festlegung des Passwortencoders
     * Nutzung des BCrypt Verschlüsselungalgorithmus (Ergebnis: 60bit Hash)
     * @return Objekt vom Typ BCrypt, mit dem anschließend Passwörter encoded werden
     */
    @Bean
    public PasswordEncoder getPasswordEncoder() {return new BCryptPasswordEncoder();}
    }

