package de.HTW.OpalDruckauftrag.configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import de.HTW.OpalDruckauftrag.entities.opal.Job;
import de.HTW.OpalDruckauftrag.repositories.opal.JobRepository;
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
 * durch folgende Funktionen für die  Datenbank "Opal-Druckauftrag" angepasst werden
 * Die Funktionen dieser Klasse sind mit der Annotation "@Primary" gekennzeichnet, da die Spring Configuration eine primäre Datenbankverbindung verlangt
 * und die Datenbank "Opal-Druckauftrag" sich dafür anbietet
 */

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = JobRepository.class, entityManagerFactoryRef = "opalDSEmFactory", transactionManagerRef = "opalDSTransactionManager")
public class OpalDBConfiguration {

    /**
     * Erstellung einer Datenbankverbindung zur Datenbank "Opal-Druckauftrag" (Benötigt für die Verwaltung der Druckjobs)
     * @return Datenbankverbindung
     */
    @Primary
    @Bean
    public DataSource opalDS() {
        return DataSourceBuilder.create().driverClassName("org.mariadb.jdbc.Driver").
                url("jdbc:mariadb://localhost:3306/Opal-Druckauftrag-DB").
                username("root").
                password("Opal-Druckauftrag2020").
                build(); }

    /**
     * Erstellung einer EntityManagerFactory für Entitäten der Datenbank "Opal-Druckauftrag"
     * @param opalDS Datenbankverbindung zu "Opal-Druckauftrag"
     * @param builder notwendig zur Erstellung
     * @return EntityManagerFactory
     */
    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean opalDSEmFactory(@Qualifier("opalDS") DataSource opalDS, EntityManagerFactoryBuilder builder) {
        return builder.dataSource(opalDS).packages(Job.class).build();
    }

    /**
     * Erstellung eines Transaktionsmanager für die Datenbank "Opal-Druckauftrag"
     * @param opalDSEmFactory EntityManagerFactory für die Datenbank "Opal-Druckauftrag"
     * @return Transaktionsmanger
     */
    @Primary
    @Bean
    public PlatformTransactionManager opalDSTransactionManager(EntityManagerFactory opalDSEmFactory) {
        return new JpaTransactionManager(opalDSEmFactory);
    }
}
