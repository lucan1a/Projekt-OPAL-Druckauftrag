package de.HTW.OpalDruckauftrag.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service zum Versenden von E-Mails (Sperrung/Entsperrung) via SMTP vom HTW-Groupware Konto des Druckservices
 * @author Felix Müller
 * @version 1.2
 */
@Service
public class MailService {

    private JavaMailSender mailSender;

    private String block=
    "Liebe OPAL-Nutzerin, lieber OPAL-Nutzer,\n" +
    "Sie haben vor kurzem einen kostenpflichtigen \"OPAL-Druckauftrag\" gestartet.\n" +
    "Dieser wurde von der UNIdruckerei bearbeitet.\n" +
    "Wegen aufgetretener \"Unregelmäßigkeiten\" wurden Sie für den Dienst \"OPAL-Druckauftrag\" gesperrt.\n" +
    "Bei Fragen zum \"OPAL-Druckauftrag\" wenden Sie sich bitte an support-ecampus@htw-dresden.de.";


    private String unblock  =
    "Liebe OPAL-Nutzerin, lieber OPAL-Nutzer,\n" +
    "Sie wurden von einem Mitarbeiter der UNIdruckerei für den \"OPAL-Druckauftrag\" freigeschaltet\n" +
    "Bei Fragen zum \"OPAL-Druckauftrag\" wenden Sie sich bitte an support-ecampus@htw-dresden.de.";

    private Logger log;

    /**
     * Konstruktor autowired
     * @param mailSender JavaMailSender Instanz
     */
    @Autowired
    public MailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
        log = Logger.getLogger(MailService.class.getName() );
        log.info("Konstruktor wurde gerufen");
    }

    /**
     * Sperrung
     * Erzeugt SimpleMailMessage und verschickt sie an das HTW-Groupware Konto des Benutzer
     * @param bibliotheksnummer sNummer des Benutzers
     */
    //todo nach dem testen Funktion wieder einfügen
    public void sendBlockNotification(String bibliotheksnummer){
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(bibliotheksnummer + "@htw-dresden.de");
        mail.setFrom("druckauftrag.opal@htw-dresden.de");
        mail.setSubject("TEST");
        mail.setText(block);

        try {
            mailSender.send(mail);
        }catch(MailException me){
            log.log(Level.SEVERE, "E-Mail an " +  bibliotheksnummer + " konnte nicht versendet werden");
        }
    }

    /**
     * Entsperrung
     * Erzeugt SimpleMailMessage und verschickt sie an das HTW-Groupware Konto des Benutzers
     * @param bibliohteksnummer sNummer des Benutzers
     */
    //todo nach dem testen Funktion wieder einfügen
    public void sendUnblockNotification(String bibliohteksnummer){
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(bibliohteksnummer + "@htw-dresden.de");
        mail.setFrom("druckauftrag.opal@htw-dresden.de");
        mail.setSubject("TEST");
        mail.setText(unblock);

        try {
            mailSender.send(mail);
        }catch(MailException me){
            log.log(Level.SEVERE, "E-Mail an " + bibliohteksnummer + " konnte nicht versendet werden");
        }
    }
}
