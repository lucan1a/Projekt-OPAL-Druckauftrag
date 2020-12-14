package de.HTW.OpalDruckauftrag.entities.opal;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * User-Entity ermöglicht OR-DB-Mapping auf Tabelle Benutzer
 * String bibliotheksnummer als Id annotiert, alle Entity-Daten als Column(DB) annotiert
 * @author Francesco Ryplewitz
 * @version 1.0
 */
@Entity
@Table(name = "Benutzer")
public class Benutzer {

    @Id
    @Column(name = "Bibliotheksnummer")
    private String bibliotheksnummer;

    @Column(name ="Sperrung")
    private byte sperrung;

    @Column(name = "Vorname")
    private String vorname;

    @Column(name = "Nachname")
    private String nachname;

    /**
     * Spring-Data-JPA benötigt leeren Constructor
     */
    public Benutzer(){}

    /**
     * Constructor mit Parameterübergabe
     * @param bibliotheksnummer SNummer des Benutzers
     * @param sperrung ORM: byte (FALSE(0) - nicht gesperrt, TRUE(1) - gesperrt)
     * @param vorname Vorname des Benutzers
     * @param nachname Nachname des Benutzers
     */
    public Benutzer(String bibliotheksnummer, byte sperrung, String vorname, String nachname) {
        this.bibliotheksnummer = bibliotheksnummer;
        this.sperrung = sperrung;
        this.vorname = vorname;
        this.nachname = nachname;
    }

    /**
     * Getter Bibliotheksnummer
     * @return Bibliotheksnummer (String)
     */
    public String getBibliotheksnummer() {
        return bibliotheksnummer;
    }

    /**
     * Getter Sperrvermerk
     * @return Sperrvermerk (byte durch ORM)
     */
    public byte getSperrung() {
        return sperrung;
    }

    /**
     * Getter Vorname
     * @return Vorname (String)
     */
    public String getVorname() {
        return vorname;
    }

    /**
     * Getter Nachname
     * @return nachname (String)
     */
    public String getNachname() {
        return nachname;
    }

    /**
     * Setter Bibliotheksnummer
     * @param bibliotheksnummer (String)
     */
    public void setBibliotheksnummer(String bibliotheksnummer) {
        this.bibliotheksnummer = bibliotheksnummer;
    }

    /**
     * Setter Sperrvermerk
     * @param sperrung (als byte durch ORM)
     */
    public void setSperrung(byte sperrung) {
        this.sperrung = sperrung;
    }

    /**
     * Setter Vorname
     * @param vorname (String)
     */
    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    /**
     * Setter Nachname
     * @param nachname (String)
     */
    public void setNachname(String nachname) {
        this.nachname = nachname;
    }
}
