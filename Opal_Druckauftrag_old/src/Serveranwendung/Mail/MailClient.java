import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.Folder;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.FLAGS;  
import org.apache.pdfbox.pdmodel.PDDocument;

/** 
 * Klasse MailClient fuer das Versenden und Empfangen von Emails mittels javax.mail
 * @author fewihu
 * @author pauljannasch
 */

public class MailClient {

    /** Maileinstellung */
    public String mailType;
    /** Maileinstellung */
    public String saveDirectory = "/home/druckauftrag/Dateien/raw/";
    /** Maileinstellung */
    public int mailCount;
    /** Servereinstellungen */
    public String file;
    /** Servereinstellungen */
    public String fileName;

    /** Datenbankparameter */
    public String einstellungen;
    /** Datenbankparameter */
    public String bibliotheksnummer;
    /** Datenbankparameter */
    public long fileSize;
    /** Datenbankparameter */
    public long numberOfPages;
    /** Datenbankparameter */
    public String error;

    /** Connection Einstellungen */
    public String host;
    /** Connection Einstellungen */
    public int port;
    /** Connection Einstellungen */
    private static String account;
    /** Connection Einstellungen */
    private static String password;

    /** Connection Einstellungen */
    private static String sender;
    /** Connection Einstellungen */
    private static String recipient;

    /** Connection Einstellungen */
    private static String subject;
    /** Connection Einstellungen */
    private static String content;

    /** javax mail Configuration */
    public Properties properties;
    /** javax mail Configuration */
    public Session session;

    /** javax mail Configuration */
    public Store store;
    /** javax mail Configuration */
    public Folder inbox;

    /**
     * Konstruktor
     */
    MailClient() {
        this.mailType = "none";
    }

    /**
     * Konstruktor fuer einen MailClient mit Typ und recipient
     * @param mailType
     * @param recipient
     */
    MailClient(String mailType, String recipient, String fileName) {
        this.mailType = mailType;
        this.recipient = recipient;
        this.fileName = fileName;
    }

    /**
     * Constructor fuer Fehelrmails
     */
    MailClient(String mailType, String recipient, String error, String fileName) {
        this.mailType = mailType;
        this.recipient = recipient;
        this.error = error;
        this.fileName = fileName;
    }

    /**
     * Initialise die Verbindungseinstellungen fuer das Senden von Mails
     */
    public void initSend() {

        /**
         * check ob die Maileinstellungen valid sind
         * setzen des Mail-Contents nach Typ
         */
        if(!this.mailType.matches("confirm|error|sperren|erinnern|druckerei")) {
            System.out.println("wrong or no mail type defined.");
            System.out.println("types are: confirm | error | sperren | erinnern | druckerei");
        } else {
            switch(mailType) {
                case "confirm": this.subject = "Bestaetigung ihres Druckauftrags"; break;
                case "error": this.subject = "Fehler bei ihrem Druckauftrag"; break;
                case "erinnern": this.subject = "Erinnerung an ihren Druckauftrag"; break;
            }
            
            if(this.mailType.equals("error")) 
                this.content = setCustomContent(mailType, error, fileName);
            else
                this.content = setCustomContent(mailType, "", fileName);
        }

        /**
         * host und port des absenders
         */
        this.host = "groupware.htw-dresden.de";
        this.port = 587;

        /**
         * Properties definieren fuer die Mail-Verbindung
         */
        this.properties = new Properties();
        this.properties.put("mail.smtp.auth", "true");
        this.properties.put("mail.smtp.starttls.enable", "true");
        this.properties.put("mail.smtp.ssl.trust", this.host);
        this.properties.put("mail.smtp.host", this.host);
        this.properties.put("mail.smtp.port", this.port);

        /**
         * Email Account Daten setzen und Session erstellen
         */
        this.account = "druckauftrag_opal";
        this.password = "Z604_Z832";

        this.session = Session.getInstance(properties, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(account, password);
            }
        });

        this.sender = "druckauftrag.opal@htw-dresden.de";
    }

    /**
     * Initialise die Connection fuer den IMAPListener
     * @throws Exception
     */
    public void initListen() throws Exception {

        /**
         * Die Verbindung zum auszulesenden Email-Konto aufbauen
         */
        this.host = "groupware.htw-dresden.de";
        this.port = 993;

        this.account = "druckauftrag_opal";
        this.password = "Z604_Z832";

        /**
         * connect zut imap inbox
         * Properties definieren fuer die Mail-Verbindung
         */
        this.properties = new Properties();
        this.properties.put("mail.imap.auth", "true");
        this.properties.put("mail.store.protocol", "imaps");
        this.properties.put("mail.imap.startssl.enable", "true");
        this.properties.put("mail.imap.user", this.account);
        this.properties.put("mail.imap.host", this.host);
        this.properties.put("mail.imap.port", this.port);
        this.properties.put("mail.imaps.partialfetch", "false");

        /**
         * neue Session mit den Properties erstellen
         */
        Session session = Session.getInstance(properties, new Authenticator(){

            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(account, password);
            }
        });

        this.store = session.getStore("imaps");
        store.connect(host, account, password);

	    this.inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_WRITE);
    }

    /**
     * Einfache Email Message Anhang vorbereiten
     * @return Message
     */
    private Message prepareMessage() {

        try {

            Message message = new MimeMessage(this.session);

            /**
             * Absender, Empfaenger und Betreff setzen
             */
            message.setFrom(new InternetAddress(this.sender));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(this.recipient));
            message.setSubject(this.subject);

            /**
             * Setzen des Email Inhalts
             */
            message.setContent(this.content, "text/plain");
            return message;

        } catch(MessagingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * parsen der Mailnachricht nach der Bibliotheksnummer des Nutzers
     * @param content
     * @return S-Nummer
     */
    public String getSnr(String content) {

        /**
         * parsen des Content Strings nach dem Pattern der Mail
         */
        String snr = "s";
        String pattern = "die Lernplattform OPAL von s";
        int index = content.lastIndexOf(pattern);

        /**
         * Zusammensetzen der Bibliotheksnummer mittels index + patternlaenge bis + 5
         */
        for(int i = index + pattern.length(); i < index + pattern.length() + 5; i++) {
            snr += content.charAt(i);
        }

        return snr;
    }

    /**
     * parsen der Mailnachricht nach den geforderten Druckeinstellungen des Nutzers
     * @param content
     * @return einstellungen
     */
    public String getEinstellungen(String content) {

        /**
         * parsen des Content Strings nach dem Pattern der Mail
         */
        String einstellungen = "";
        String pattern = "diesen Druckauftrag wurde das Seitenlayout: ";
        int index = content.lastIndexOf(pattern);

        /**
         * nach den geforderten Einstellungen den String einstellungen fuer den Eintrag in die DB anpassen
         */
        char read = content.charAt(index + pattern.length());
        switch (read) {
            case '1': einstellungen = "11"; break;
            case '2': einstellungen = "12"; break;
            case '4': einstellungen = "22"; break;
            default: break;
        }

        return einstellungen;
    }

    /**
     * Funktion um die Anzahl aer Mails die momentan im Posteingang liegen zu bekommen
     * @return numberOfMails
     */
    public int getMailCount() {

        int count = 0;

        try {

            this.initListen();
            Message[] messages = this.inbox.getMessages();
            count = messages.length;

            this.inbox.close(true);
            this.store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Senden einer Email an den Empfaenger
     * @return 0: success
     */
    public int sendMail() {

        /**
         * initialise Connection
         */
        this.initSend();

        /**
         * Vorbereitung der Message fuer das Versenden
         */
        Message message = prepareMessage();

        /**
         * Versenden der Message
         */
        try {
            Transport.send(message);
        } catch(MessagingException me) {
            me.printStackTrace();
            return 1;
        }
        return 0;
    }

    /**
     * IMAP Inbox Listener with Attachments
     * https://www.codejava.net/java-ee/javamail/get-attachments-in-e-mail-messages-using-javamail
     * @param multiPart
     * @throws Exception
     * @return String MailContent
     */
    public String getMailContent(Multipart multiPart) throws Exception {

        String messageContent = "";
        String contentType = "";   
        String attachedFiles = "";
        int numberOfParts = multiPart.getCount();

        /**
         * Iteration ueber alle Teile der Mail (BodyParts)
         * fuer jeden Teil wird die Weiterverarbeitung bestimmt
         */
        for(int partCount = 0; partCount < numberOfParts; partCount++) {

            MimeBodyPart bodyPart = (MimeBodyPart) multiPart.getBodyPart(partCount);

            /**
             * Verarbeitung und speichern von Anhaengen (*.pdf in)
             * Wenn Bodypart kein Anhang ist rekursiv parsen bis der Content der Mail gefunden wird
             */
            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                String fileName = bodyPart.getFileName();
                
                attachedFiles += fileName + (", ");
                if(fileName.matches(".*\\.pdf")) {
                    
                    /**
                     * Anpassen des Filenamens wenn Sonderzeichen vorkommen
                     */
                    if(fileName.contains("UTF-8") && fileName.contains("%")) {
                        fileName = replaceName(fileName);
                    }
                    fileName = fileName.replace(" ", "_");

                    /**
                     * Anpassung des Filenamens, wenn Punkte ausserhalb der
                     * Dateiendung existieren
                     */
                    if(fileName.indexOf(".") != fileName.length() - 4) {
                        fileName = fileName.replace(".", "");
                        fileName = new String(fileName.substring(0, fileName.length() - 3) + ".pdf");
                    }

                    this.file = fileName;
                    File file = new File(this.saveDirectory + File.separator + fileName);
                    this.fileSize = file.length();

                    /**
                     * speichern des empfangenen Files
                     */
                    String filePath = "";
                    if(file.exists()) {
                        bodyPart.saveFile(this.saveDirectory + "_" + File.separator + fileName);
                        filePath = this.saveDirectory + "_" + File.separator + fileName;
                    } else {
                        bodyPart.saveFile(this.saveDirectory + File.separator + fileName);
                        filePath = this.saveDirectory + File.separator + fileName;
                    }
                    
                    try {
                        /**
                        * PDF Seitenzahl extrahieren mittels PDFBox
                        */
                        PDDocument doc = PDDocument.load(new File(filePath));
                        this.numberOfPages = doc.getNumberOfPages();

                        doc.close();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    
                }

            }else if (bodyPart.getContent() instanceof MimeMultipart) {
                messageContent = getMailContent((Multipart) bodyPart.getContent());
            } else {
                messageContent = bodyPart.getContent().toString();
            }
        }
        if (attachedFiles.length() > 1) {
            attachedFiles = attachedFiles.substring(0, attachedFiles.length() - 2);
        }
        return messageContent;
    }

    /**
     * Funktion fuer die Auslesung des Posteingangs
     * liest immer nur eine Mail und verarbeitet den Content + Anhang
     * @return if success
     */
    public boolean IMAPListener() {

        try {

            this.initListen();
            Message[] messages = this.inbox.getMessages();
            this.mailCount = messages.length;

            if(messages.length == 0)
                System.out.println("No messages found.");

            Message message = messages[0]; 
            String contentType = message.getContentType();
            String messageContent = "";

            /**
             * Verarbeitung des Contents -> siehe getContent()
             */
            if(contentType.contains("multipart")) {
                Multipart multiPart = (Multipart) message.getContent();
                messageContent = getMailContent(multiPart);
            } else if(contentType.contains("text/plain") || contentType.contains("text/html")) {
                Object content = message.getContent();
                if (content != null) {
                    messageContent = content.toString();
                }
            }

            /**
             * setzen der Maileigenschaften des Nutzers
             */
            this.bibliotheksnummer = this.getSnr(messageContent);
            this.einstellungen = this.getEinstellungen(messageContent);

            /**
             * bei der letzten Message werden alle momentanen Messages in der Inbox
             * geloescht mittels DELETED Flag
             */
            messages[0].setFlag(FLAGS.Flag.DELETED, true);  
            System.out.println("Mail wurde gelöscht.");

            this.inbox.close(true);
            this.store.close();

            return true;
            
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * einfache String Funktion
     * angepasst um moegliche Sonderzeichen im Namen zu eleminieren
     * @param fileName
     * @return new fileName
     */
    public String replaceName(String fileName) {
        fileName = fileName.replace("UTF-8", "");
        fileName = fileName.replace("'", "");

        fileName = fileName.replace("%C3%A4", "ae");
        fileName = fileName.replace("%C3%84", "Ae");

        fileName = fileName.replace("%C3%B6", "oe");
        fileName = fileName.replace("%C3%96", "Oe");

        fileName = fileName.replace("%C3%BC", "ue");
        fileName = fileName.replace("%C3%9C", "Ue");

        fileName = fileName.replace("%C3%9F", "ss");

        fileName = fileName.replace("%20", "_");

        return fileName;
    }

    /**
     * Setzt den Inhalt einer zu versendenden Mail je nach Typ
     * Bei Typ error muss der Fehler mit uebergeben werden
     * @param type
     * @param error
     * @param fileName
     * @return Inhalt
     */
    public String setCustomContent(String type, String error, String fileName) {
        String content = "";
        switch(type) {
            case "confirm": content = "Liebe OPAL-Nutzerin, lieber OPAL-Nutzer,\n\n" +

                "Sie haben vor kurzem einen kostenpflichtigen 'OPAL-Druckauftrag' gestartet.\n" +
                "Dieser wird jetzt von der UNIdruckerei bearbeitet.\n\n" +

                "Es handelt sich um folgende Datei: " + fileName + "\n\n" +

                "Alle durch Sie bis Mitternacht erteilten Druckauftraege werden gesammelt und im\n" +
                "Format A4 - in Graustufen umgewandelt, doppelseitig, an der langen Seite links\n" +
                "zweifach gelocht - ausgedruckt.\n" +
                "Ihr Auftrag ist am folgenden Tag ab 09:00 Uhr in der UNIdruckerei, Reichenbachstr. 19\n" +
                "abholbereit.\n\n" +

                "Wenn eine postalische Zusendung gewuenscht wird, so schreiben Sie eine e-mail an\n" +
                "HTW@UNIdruckerei.de<mailto:HTW@UNIdruckerei.de> mit ihrer Bibliotheksnummer\n" +
                "(s-Nummer) und ihrer vollstaendigen Postanschrift.\n\n" +

                "Sie erhalten dann eine Rechnung inkl. der Versandkosten zur Vorabueberweisung.\n" +
                "Kostenuebernahme:\n" +
                "Die Kosten belaufen sich auf 5 ct pro Druckseite und sind in Summe in bar bzw.\n" +
                "mittels Geldkarte in der UNIdruckerei zu begleichen oder bei postalischer Zusendung\n" +
                "zu ueberweisen.\n\n" +

                "Datenschutz:\n" +
                "Ihre Druckdatei(en) und Ihre Bibliotheksnummer werden ausschliesslich fuer den Zweck\n" +
                "des Ausdrucks an die UNIdruckerei weiter gegeben.\n\n" +

                "Mit der Ausloesung eines Druckauftrages (Button: 'Druckauftrag abschicken') geben Sie\n" +
                "Ihre ausdrueckliche Zustimmung zur Kostenuebernahme und zum Datenschutz.\n\n" +

                "Bitte beachten Sie folgende Hinweise:\n" +
                "- Viele Dozentinnen und Dozenten stellen ihre Unterlagen erst unmittelbar nach\n" +
                "Start der Vorlesung bereit.\n" +
                "- Der Ausdruck passwortgeschuetzter PDF-Dokumente ist technisch bedingt nicht moeglich.\n" +
                "- Druckauftraege, deren Dateigroesse 20 MByte uebersteigt koennen gegenwaertig nicht\n" +
                "verarbeitet werden.\n\n" +

                "Bei Fragen zum 'OPAL-Druckauftrag' wenden Sie sich bitte an\n" +
                "support-ecampus@htw-dresden.de<mailto:support-ecampus@htw-dresden.de>.";
                break;

            case "error": content = "Liebe OPAL-Nutzerin, lieber OPAL-Nutzer,\n\n" +

                "Sie haben vor kurzem einen kostenpflichtigen 'OPAL-Druckauftrag' gestartet.\n" +
                "Bei der Verarbeitung ist es zu einem technischen Fehler gekommen.\n\n" +

                "Es handelt sich um folgende Datei: " + fileName + "\n\n" +
                "Fehlerursache: " + error + "\n\n" +

                "Bitte wenden Sie sich an\n" +
                "support-ecampus@htw-dresden.de<mailto:support-ecampus@htw-dresden.de>.";
                break;

            case "erinnerung": content = "Liebe OPAL-Nutzerin, lieber OPAL-Nutzer,\n\n" +

                "Sie haben vor kurzem einen kostenpflichtigen 'OPAL-Druckauftrag' gestartet.\n" +
                "Dieser wurde von der UNIdruckerei bearbeitet.\n" +
                "Bitte holen sie diesen innerhalb der naechsten drei Werktage in der UNIdruckerei ab.\n\n" +

                "Bei Fragen zum 'OPAL-Druckauftrag' wenden Sie sich bitte an\n" +
                "support-ecampus@htw-dresden.de<mailto:support-ecampus@htw-dresden.de>.";
                break;

            default: break;
        }

        return content;
    }

}
