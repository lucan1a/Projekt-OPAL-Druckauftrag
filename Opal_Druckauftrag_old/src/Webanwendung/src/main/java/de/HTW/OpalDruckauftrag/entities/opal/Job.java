package de.HTW.OpalDruckauftrag.entities.opal;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * Job-Entity ermöglicht OR-DB-Mapping auf Tabelle Druckjob
 * short Job_ID als ID annotiert, alle Entity-Daten als Column(DB) annotiert
 * @author Felix Müller
 * @version 1.0
 */
@Entity
@Table(name = "Druckjob")
public class Job{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private short Job_ID;

    @Column(name="Auftraggeber_Bibliotheksnummer")
    private String auftraggeber_Bibliotheksnummer;

    @Column(name="Seitenzahl")
    private short seitenzahl;

    @Column(name="Preis")
    private BigDecimal preis;

    @Column(name="Druckstatus")
    private byte druckstatus;

    @Column(name="Auftragsdatum")
    private Date auftragsdatum;

    @Column(name ="Printed")
    private byte printed;

    /**
     * Constructor mit Parameterübergabe
     * @param job_ID DruckjobID
     * @param auftraggeber_Bibliotheksnummer SNummer des Auftraggebers
     * @param seitenzahl Seitenzahl des Dokuments
     * @param preis Preis für Druckjob
     * @param druckstatus false(0) - nicht gedruckt, true(1) gedruckt
     * @param auftragsdatum bei Zusammenfassen der Aufträge zu Druckjob von DB generiert
     */
    public Job(short job_ID, String auftraggeber_Bibliotheksnummer, short seitenzahl, BigDecimal preis, byte druckstatus, Date auftragsdatum, byte printed) {
        this.Job_ID = job_ID;
        this.auftraggeber_Bibliotheksnummer = auftraggeber_Bibliotheksnummer;
        this.seitenzahl = seitenzahl;
        this.preis = preis;
        this.druckstatus = druckstatus;
        this.auftragsdatum = auftragsdatum;
        this.printed = printed;
    }

    /**
     * Spring-Data-JPA benötigt leeren Constructor
     */
    public Job(){}



    /**
     * Getter Job_ID
     * @return Jod_ID (short)
     */
    public short getId() {
        return Job_ID;
    }

    /**
     * Setter Job_ID
     * @param id Job_ID (short)
     */
    public void setId(short id) {
        this.Job_ID = id;
    }

    /**
     * Getter Bibliotheksnummer
     * @return auftraggeber_Bibliotheksnummer (String)
     */
    public String getAuftraggeber_Bibliotheksnummer() {
        return auftraggeber_Bibliotheksnummer;
    }

    /**
     * Setter Bibliohteksnummer
     * @param bibliotheksnummer_auftraggeber (String)
     */
    public void setAuftraggeber_Bibliotheksnummer(String bibliotheksnummer_auftraggeber) {
        this.auftraggeber_Bibliotheksnummer = bibliotheksnummer_auftraggeber;
    }

    /**
     * Getter Seitenzahl
     * @return seitenzahl (short)
     */
    public short getSeitenzahl() {
        return seitenzahl;
    }

    /**
     * Setter Seitenzahl
     * @param seitenzahl (short)
     */
    public void setSeitenzahl(short seitenzahl) {
        this.seitenzahl = seitenzahl;
    }

    /**
     * Getter Preis
     * @return preis (BigDecimal)
     */
    public BigDecimal getPreis() {
        return preis;
    }

    /**
     * Setter Preis
     * @param preis (decimal)
     */
    public void setPreis(BigDecimal preis) {
        this.preis = preis;
    }

    /**
     * Getter Druckstatus
     * @return druckstatus (byte)
     */
    public byte getDruckstatus() {
        return druckstatus; }

    /**
     * Setter Druckstatus
     * @param druckstatus (byte)
     */
    public void setDruckstatus(byte druckstatus) {
        this.druckstatus = druckstatus;
    }

    /**
     * Getter Auftragsdatum
     * @return Auftragsdatum (Date)
     */
    public Date getAuftragsdatum() {
        return auftragsdatum;
    }

    /**
     * Setter Auftragsdatum
     * @param auftragsdatum (Date)
     */
    public void setAuftragsdatum(Date auftragsdatum) {
        this.auftragsdatum = auftragsdatum;
    }

    /**
     * Getter Printed-Status(ist der Druckjob gedruckt)
     * @return byte
     */
    public byte getPrinted() {
        return printed;
    }

    /**
     * Setter Printed-Status(Druckjob ist gedruckt)
     * @param printed (byte)
     */
    public void setPrinted(byte printed) {
        this.printed = printed;
    }
}
