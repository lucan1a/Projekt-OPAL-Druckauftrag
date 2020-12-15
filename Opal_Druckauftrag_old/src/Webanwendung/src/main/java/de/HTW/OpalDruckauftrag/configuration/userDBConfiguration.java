package de.HTW.OpalDruckauftrag.configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import de.HTW.OpalDruckauftrag.entities.user.User;
import de.HTW.OpalDruckauftrag.repositories.user.UserReporitory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Für die Gewährleistung von zwei Datenbankverbindungen im Projekt, muss die sonst automatische Spring Configuration
 * durch folgende Funktionen für die  Datenbank "WebSecurity" angepasst werden
 */

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = UserReporitory.class, entityManagerFactoryRef = "userDSEmFactory", transactionManagerRef = "userDSTransactionManager")
public class userDBConfiguration {

    /**
     * Erstellung einer Datenbankverbindung zur Datenbank "WebSecurity" (Benötigt für den Passwortschutz)
     * @return Datenbankverbindung
     */
    @Bean
    public DataSource userDS() {
        return DataSourceBuilder.create().driverClassName("org.mariadb.jdbc.Driver").
                url("jdbc:mariadb://localhost:3306/WebSecurity").
                username("root").
                password("Opal-Druckauftrag2020").
                build(); }

    /**
     * Erstellung einer EntityManagerFactory für Entitäten der Datenbank "WebSecurity"
     * @param userDS Datenbankquelle
     * @param builder notwendig zur Erstellung
     * @return EntityManagerFactory
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean userDSEmFactory(@Qualifier("userDS") DataSource userDS, EntityManagerFactoryBuilder builder) {
        return builder.dataSource(userDS).packages(User.class).build();
    }

    /**
     *Erstellung eines Transaktionsmanager für die Datenbank "WebSecurity"
     * @param userDSEmFactory EntityFactoryManger für die Datenbank "Websecurity"
     * @return Transaktionsmanger
     */
    @Bean
    public PlatformTransactionManager userDSTransactionManager(EntityManagerFactory userDSEmFactory) {
        return new JpaTransactionManager(userDSEmFactory);
    }
}
