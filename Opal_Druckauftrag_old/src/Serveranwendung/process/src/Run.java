import java.io.File;
import java.util.Arrays;
import java.util.List;

import java.lang.ProcessBuilder;
import java.io.*;
import java.io.File;

import java.time.LocalTime;

/**
 * Abbild des Lauffaehigen Gesamtprogrammes
 * @author pauljannasch 
 */

public class Run {

    /**
     * MailClient als Schnittstelle zum Postfach
     */
    public static MailClient mailClient;


    /** 
     * Verarbeitung eines Dokuments in die gewuenschte Form mittels Shell-Skripten
     * @param bibliotheksnummer Bibliotheksnummer des Studenten
     * @param numberOfPages Seitenanzahl des Dokuments
     * @param einstellungen geforderte Einstellungen
     * @param fileSize Dateigroesse
     * @param fileName Dateiname
     * @return 0 if success, not 0 else
     */
    public static int processFile(String bibliotheksnummer, int numberOfPages, int einstellungen, long fileSize, String fileName) {

        String file = "/home/druckauftrag/Dateien/raw/" + fileName;
        System.out.println("neuer Druckauftrag: " + file);
        int ret;
        int deleteAuftrag;
        
        int dividentPages = 1;
        switch(einstellungen) {
            case 11: break;
            case 12: dividentPages = 2; break;
            case 22: dividentPages = 4; break;
            default: break;
        }

        double bearbeitet = (double) numberOfPages / dividentPages;
        
        Benutzer benutzer = new Benutzer(bibliotheksnummer, false, "Vorname", "Nachname");
        Druckauftrag druckauftrag = new Druckauftrag(0, bibliotheksnummer, false, numberOfPages, (int) Math.ceil((double) bearbeitet), einstellungen, file.substring(31), (int) fileSize);

        /* Pruefung, ob Datei heute schon von Student abgeschickt wurde */
         if(druckauftrag.countDateinameBenutzer()>0){
             System.out.println("Datei "+fileName+ " von "+bibliotheksnummer+ "heute schon verarbeitet. Es wird kein neuer Auftrag generiert!");
             File rawfile = new File(file);
             rawfile.delete();
             return 5;
         }

        /** 
          * check ob der Benutzer schon in der Datenbank steht, sonst einfuegen
          */
        if(benutzer.exists()) {
            System.out.println("Benutzer bereits vermerkt.");
            if(benutzer.getSperrstatus()) {
                System.out.println("Nutzer ist gesperrt. Abbruch.");
                mailClient = new MailClient("error", bibliotheksnummer + "@htw-dresden.de", 
                    "Sie sind momentan fuer Druckauftraege gesperrt, sollte es sich hierbei um einen Fehler handeln, so melden sie sich bitte bei der UNIdruckerei", fileName);
                mailClient.sendMail();
                return 1;
            }
        } else {
            ret = benutzer.insert();
            System.out.println("new Benutzer.insert() returns: " + ret);
        }


        /** 
         * vermerk der Daten in Tabelle Druckauftrag
         */
        ret = druckauftrag.insert();
        druckauftrag.setID();
        System.out.println("new druckauftrag.insert() returns: " + ret);

        //------------------------------------------------------------------------------//

        System.out.println("starte pdf-verarbeitung...");
        /** 
         * Verarbeitung der PDFs
         * run processes on new file
         */
        ProcessHandler processHandler = new ProcessHandler();
        String[] parameter;

        /** 
         * check for password
         */
        System.out.println("checking for password...");
        parameter = new String[]{file};
        processHandler.runScript(processHandler.checkPassword, parameter);

        if(processHandler.exitValue == 1) {
            System.out.println("send error mail: passwortgeschuetzt an " + bibliotheksnummer + "@htw-dresden.de");
            mailClient = new MailClient("error", bibliotheksnummer + "@htw-dresden.de", "PDF ist Passwortgeschuetzt.", fileName);
            mailClient.sendMail();

            deleteAuftrag = druckauftrag.deleteElement();
            System.out.println("deleted Druckauftrag, ret: " + deleteAuftrag);

            return 1;
        }

        /**
         * convert into grey Version
         */
        System.out.println("grey conversion...");
        processHandler.runScript(processHandler.convertGrey, parameter);

        if(processHandler.exitValue != 0) {
            System.out.println("send error mail: grey an " + bibliotheksnummer + "@htw-dresden.de");
            mailClient = new MailClient("error", bibliotheksnummer + "@htw-dresden.de", 
                "Die PDF kann nicht in Graustufen umgewandelt werden.", fileName);
            mailClient.sendMail();

            deleteAuftrag = druckauftrag.deleteElement();
            System.out.println("deleted Druckauftrag, ret: " + deleteAuftrag);

            return 2;
        }

        /** 
         * convert zu mehreren folien pro Seite
         */
        System.out.println("convert pages...");

        /** 
         * einstellungen extrahieren mittels % 10
         * 22 steht zum Beispiel fuer 2x2
         */
        int line = einstellungen % 10;
        einstellungen /= 10;
        int col = einstellungen;
        
        System.out.println("convertiere zu format " + col + "x" + line);

        parameter = new String[]{
            file, 
            String.valueOf(col), 
            String.valueOf(line), 
            druckauftrag.Auftraggeber_Bibliotheksnummer, 
            String.valueOf(druckauftrag.Auftrags_ID)
        };
        processHandler.runScript(processHandler.convertPages, parameter);

        if(processHandler.exitValue != 0) {
            System.out.println("send error mail: convert an " + bibliotheksnummer + "@htw-dresden.de");
            mailClient = new MailClient("error", bibliotheksnummer + "@htw-dresden.de", 
                "Die Seiten der PDF koennen nicht zusammengefasst werden.", fileName);
            mailClient.sendMail();

            deleteAuftrag = druckauftrag.deleteElement();
            System.out.println("deleted Druckauftrag, ret: " + deleteAuftrag);
            
            return 3;
        }

        /** 
         * Hinzufuegen einer leeren Seite bei ungerader Anz. Seiten nach Bearbeitung:
         * Test ob aufgerundete Anz. Seiten durch zwei teilbar - wenn nicht:
         * Skript combine starten und Auftrag mit leerer PDF-Seite zusammenfuegen
         */
         
        if((int) Math.ceil((double) bearbeitet) % 2 != 0) {

            //{snr,pdf,leeres pdf,paramter fuer combine}
            parameter = new String[]{
                druckauftrag.Auftraggeber_Bibliotheksnummer,
                "/home/druckauftrag/Dateien/preprocessed/" + druckauftrag.Auftraggeber_Bibliotheksnummer +"_"+ druckauftrag.Auftrags_ID +"_"+ fileName,
                "/home/druckauftrag/src/pdf_handling/final/leeres_pdf.pdf", 
                "empty_pdf"  
            };
            
            processHandler.runScript(processHandler.combine, parameter);

            if(processHandler.exitValue != 0) {
                System.out.println("send error mail: convert an " + bibliotheksnummer + "@htw-dresden.de");
                mailClient = new MailClient("error", bibliotheksnummer + "@htw-dresden.de", 
                    "Die Seiten der PDF koennen nicht zusammengefasst werden.", fileName);
                mailClient.sendMail();

                deleteAuftrag = druckauftrag.deleteElement();
                System.out.println("deleted Druckauftrag, ret: " + deleteAuftrag);
                
                return 4;
            }
        }

        return 0;
    }
    /**
    * Runtime vom Programm
    * Ausgabe wenn neuer File im Verzeichnis vorliegt
    * jobsDone: true wenn die jobs des Tages verarbeitet wurden, 
    * workingJobs: true wenn die jobs gerade verarbeitet werden
    */
    public static void main(String[] args) throws Exception {

        boolean jobsDone = true;
        boolean workingJobs = false;
  
        Druckjob druckjob = new Druckjob();

        while(true) {

            if(workingJobs == false) {

                /**
                 * running im Normalbetrieb und verarbeiten neuer PDFs
                 */
                int mainMailCount = 0;

                try {

                    /**
                     * numberOfMails: Anzahl von Mails die momentan im Posteingang liegen
                     * deleteFlag: true wenn alle numberOfMails geparst wurden und geloescht werden koennen
                     */
                    mailClient = new MailClient();
                    int numberOfMails = mailClient.getMailCount();
                    boolean deleteFlag = false;
                    
                    while(numberOfMails > 0) {

                        /**
                         * IMAPListener() triggered wenn neue Mails im Posteingang liegen
                         */
                        mailClient.IMAPListener();

                        System.out.println("New Mail.");
                        System.out.println("bibliotheksnummer: " + mailClient.bibliotheksnummer +
                                " einstellungen: " + mailClient.einstellungen +
                                " numberOfPages: " + mailClient.numberOfPages +
                                " file: " + mailClient.file + 
                                " fileSize: " + mailClient.fileSize);

                        int retFile = processFile(mailClient.bibliotheksnummer, (int) mailClient.numberOfPages, 
                                Integer.parseInt(mailClient.einstellungen), mailClient.fileSize, mailClient.file);
                        System.out.println("File verarbeitung returned: " + retFile);



                        /**
                         * senden der Bestaetigungsmail sobald die Verarbeitung abgeschlosssen ist
                         */
                        if(retFile != 5){
                            mailClient = new MailClient("confirm", mailClient.bibliotheksnummer + "@htw-dresden.de", mailClient.file);
                            mailClient.sendMail();
                            System.out.println("Auftrag wurde per Mail bestätigt");
                        }

                        numberOfMails = mailClient.getMailCount();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Thread.sleep(2000);
            }
            
            /**
             * vor 1:00 Uhr werden die Jobs auf nicht erledigt gesetzt
             */
            if(LocalTime.now().isBefore(LocalTime.parse("01:00")) && jobsDone == true) {
                jobsDone = false;

                /**
                 * Parsen nach unagbeholten Druckjobs = 7 Tage
                 */
                String[] erinnerung = druckjob.getAllOverdueJobs();

                /**
                 * senden aller Erinnerungsmails fuer alle gefundenen Druckjobs
                 */
                for(int i = 0; i < erinnerung.length; i++) {
                    String studentMail = erinnerung[i] + "@htw-dresden.de";
                    System.out.print(studentMail + ", ");

                    mailClient = new MailClient("erinnerung", studentMail, "");
                    mailClient.sendMail();
                }
            }

            /**
             * nach 5:00 Uhr werden die Jobs erledigt, wenn sie false gesetzt sind
             */
            if(LocalTime.now().isAfter(LocalTime.parse("05:00")) && jobsDone == false) {

                workingJobs = true;

                System.out.println("Jobs werden verarbeitet...");
                System.out.println("Normalbetrieb eingestellt.");

                /**
                 * Ausfuehren des DailyTask
                 */
                DailyTask dailyTask = new DailyTask();
                dailyTask.run();

                System.out.println("Rueckkehr in Normalbetrieb...");

                jobsDone = true;
                workingJobs = false;
            }
        }
    }
}
