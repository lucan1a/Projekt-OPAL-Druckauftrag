import java.lang.*; //convert
import java.util.*; //timer
import java.util.Arrays;
import java.util.Date;
import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.text.*; //Formate

/**
 * Timer zum taeglichen Zusammenfassen der Druckauftraege zu einem Druckjob je Student.
 * @author Annabelle
 */
  
public class DailyTask {

    public Date today;
    public String[] sNr_auftrag;
    public Druckjob[] job;
    
    public static boolean vorhanden;
    public static int anz_dateien, anz_seiten;
    public static int exitValue;
    public static Druckauftrag auftrag;
    public static ProcessHandler endProcess;
    public static String path_preprocessed = "/home/druckauftrag/Dateien/preprocessed/";
    public static String [] parameter_combine, parameter_barcode, parameter_deckblatt;
    /** Preis pro Druckseite */
    public static double preis=0.05;  
    /** Decimalformat fuer Preis (zwei Nachkommastellen) */
    public static DecimalFormat df = new DecimalFormat("0.00");   
    /** Dateformat fuer Auftragsdatum (heutiges Datum in jahr-monat-tag)*/
    public static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
   
   /**
    * Start und Ende der Druckjobzusammenfassung
    */
    public void run() {
        
        today = new Date();

        System.out.println("");
        System.out.println("DailyTask " + today + " startet........");
        System.out.println("");

        /**Instanz von Druckauftrag zum Zugriff auf Methoden*/
        auftrag = new Druckauftrag();
        
        /**Alle uniquen s-Nummern der vorhandenen Druckauftraege aus DB holen*/
        sNr_auftrag = auftrag.getAuftragBibNr();

        /** Start Ueberpruefung auf nicht zuordbare PDF im Ordner */
        File Docs = new File(path_preprocessed);
        String allDocs [] = Docs.list();

        /**Abgleich SNrn von Auftraegen und SNrn vorhandener Dateien */
        vorhanden = false;
        for(int k=0; k<allDocs.length; k++){
            for(int l=0; l<sNr_auftrag.length; l++){
                if(allDocs[k].substring(0,6).compareTo(sNr_auftrag[l])==0){
                    vorhanden = true;
                }
            }
            /* wenn nicht zuordbare PDF gefunden - Fehlermail an Student */
            if(vorhanden==false){
                System.out.println("nicht zuordbare PDF gefunden: " + allDocs[k]);
                this.sendErrorMail(allDocs[k], allDocs[k].substring(0,6));
            }
            vorhanden=false;
        }
   
        if(sNr_auftrag.length != 0) {

            System.out.println("Auftraege von folgenden Studenten zu verarbeiten:");
            System.out.println(Arrays.toString(sNr_auftrag));
            System.out.println("");


            /**Weitergabe zur Druckjoberzeugung*/
            this.generateJobs(); 
        }
        else
            System.out.println("------------------------------keine Druckauftraege vorhanden--------------------------");
        
        
    
        /**Order preprocessed leeren */
        File file = new File(path_preprocessed);
        String[] list_files = file.list();
        
        for(int i=0; i<list_files.length; i++){
            File to_delete = new File( path_preprocessed + list_files[i]);
            if(to_delete.delete() == false) 
                System.out.println("------------Achtung: im Order 'Preprocessed' befinden sich noch Dateien!--------------");
        }
        
        System.out.println("");
        System.out.println("DailyTask beendet.........");
        System.out.println("");
    }


    /**
     * Erzeugung der Druckjobs und Weitergabe zur Verarbeitung
     */
    void generateJobs() {
    
        /**heutiges Datum fuer Auftragsdatum Druckjob holen*/
        /**Fuer jede unique s-Nummer einen Druckjob erzeugen 
         *Dazu die einzelnen Werte mit den Methoden aus Druckauftrag/Druckjob aus DB holen 
         */
        job = new Druckjob[sNr_auftrag.length];
        
        for(int i=0; i<sNr_auftrag.length; i++) {
        
            /**Druckjob(Auftraggeber_Bibliotheksnummer, Seitenzahl, Preis, Druckstatus, Auftragsdatum) */
            job[i] = new Druckjob(sNr_auftrag[i],auftrag.SeitenzahlSumme(sNr_auftrag[i]),auftrag.SeitenzahlSumme(sNr_auftrag[i])*preis, false, dateformat.format(today));
            
            /**Druckjob in DB anlegen*/
            job[i].insert();

            /**JobID holen*/
            job[i].setID();
        } 
                
            /** sNrn der Druckjobs holen fuer reihenweise Verarbeitung der Druckjobs*/
            String[] sNr_job = job[0].getBibNr();
            
            /** Start der reihenweisen Verarbeitung*/
            for(int j=0; j < sNr_job.length; j++) {
            
                if(this.processJobs(sNr_job[j],job[j])==-1){

                    /**fehlerhaften Druckjob loeschen */
                    if(job[j].delete() !=0)
                        System.out.println("------------Achtung: Druckjob "+job[j].Job_ID+" konnte nicht aus DB geloescht werden!--------------------");
                    
                } 
                System.out.println("");  
            }

            if(auftrag.delete() != 0)
                System.out.println("-----------------Achtung: Druckauftraege konnten nicht geloescht werden!----------------");       
    }


    /**
     * Reihenweise Verarbeitung der PDFs pro Druckjob
     * @param sNr_job Zugehoerige Bibliotheksnummer zum Druckjob
     * @param job Instanz des zu verarbeitenden Druckjobs
     * @return '1' fuer erfolgreiche Verarbeitung , '-1' fuer erfolglose Verarbeitung
     * @throws IOException
     */

    int processJobs(String sNr_job, Druckjob job) {

        System.out.println("------------------------------------------------------------------------------------");
        System.out.println("Verarbeitung von Druckjob: "+job.Job_ID+ " von Bib.Nr.: "+sNr_job+" startet"); 
        System.out.println("");
        
        /** ---Durchfuehrung der Verarbeitungsprozesse--- **/

        /*Erzeugung einer Instanz der Klasse ProcessHandler zur Verarbeitung der PDFs in Skripten */
        endProcess = new ProcessHandler();
                
        /**Seitenzahl und Dateianzahl fuer Skriptverarbeitung aus DB holen*/
        anz_dateien = auftrag.DateiCount(sNr_job);
        anz_seiten = auftrag.SeitenzahlSumme(sNr_job);

        try{
        /**PDFs zu Druckjob aus Verzeichnis holen*/
        String[] filesInDirectory = this.getFiles(sNr_job);

        /* Zu Druckjob in DB keine PDF gefunden - senden Fehlermail */
        if(filesInDirectory.length == 0  ){
            System.out.println("-------------Keine PDFs zu "+sNr_job+" gefunden!----------------");
            this.sendErrorMail("alle gestern abgesandten Auftraege",sNr_job);
            return -1;
        }

        /*AuftragsIDs der Dateien holen fuer Abgleich mit Datenbank */
        int [] AuftragsIDs_Dateien = new int [filesInDirectory.length] ;
    
        for(int k=0; k<AuftragsIDs_Dateien.length; k++){

            vorhanden=false;
            /* einstelliger Auftrag sxxxxx_1_x.pdf bis sxxxxx_9_x.pdf - neunte Stelle ist "_"*/
            if(filesInDirectory[k].substring(8,9).compareTo("_")==0){
                vorhanden=true;
                AuftragsIDs_Dateien[k] = Integer.parseInt(filesInDirectory[k].substring(7,8));
            }
            /* zweistelliger Auftrag 10_ bis 99_ */
            if((vorhanden==false) && (filesInDirectory[k].substring(9,10).compareTo("_")==0)){
                vorhanden=true;
                AuftragsIDs_Dateien[k] = Integer.parseInt(filesInDirectory[k].substring(7,9));
            }

            /* dreistelliger Auftrag 100_ bis 999_ */
            if((vorhanden==false) && (filesInDirectory[k].substring(10,11).compareTo("_")==0)){
                vorhanden=true;
                AuftragsIDs_Dateien[k] = Integer.parseInt(filesInDirectory[k].substring(7,10));
            }

            /* vierstelliger Auftrag 1000_ bis 9999_ */
            if((vorhanden==false) && (filesInDirectory[k].substring(11,12).compareTo("_")==0)){
                vorhanden=true;
                AuftragsIDs_Dateien[k] = Integer.parseInt(filesInDirectory[k].substring(7,11));  
            }

            /* fuenfstelliger Auftrag 10000_ bis 99999_ */
            if(vorhanden==false){
                vorhanden=true;
                AuftragsIDs_Dateien[k] = Integer.parseInt(filesInDirectory[k].substring(7,12));  
            }
        }  

        /*AuftragsIDs aus DB holen fuer Abgleich mit Dateien*/
        int [] AuftragsIDs_DB = auftrag.getAuftragID(sNr_job);
       
        vorhanden= false;
        /*Pruefen ob alle Dateien in Datenbank hinterlegt sind*/
        for(int m=0; m<AuftragsIDs_Dateien.length; m++){
            for(int n=0; n<AuftragsIDs_DB.length; n++){ 
                if(AuftragsIDs_Dateien[m]==AuftragsIDs_DB[n]){ 
                vorhanden = true;
                }
            }

            /*Wenn zu Datei kein Eintrag vorhanden ist - Loeschen und Fehlermail senden*/
            if(vorhanden == false){
                File file = new File(path_preprocessed+filesInDirectory[m]);
                file.delete();
                this.sendErrorMail(filesInDirectory[m],sNr_job);
                System.out.println("-----------------"+path_preprocessed+filesInDirectory[m] + " geloescht - nicht in DB vorhanden ---------------");
            }
            vorhanden = false;
        }
                    
        /*Pruefen ob zu jedem Datensatz Datei vorhanden ist */
                    
        for(int m=0; m<AuftragsIDs_DB.length; m++){
            for(int n=0; n<AuftragsIDs_Dateien.length; n++){ 
                if(AuftragsIDs_DB[m]==AuftragsIDs_Dateien[n]) 
                vorhanden = true;
            }

            /*Durchfuerhung bei Ungleichheit */
            if(vorhanden == false){

                /*Aktualisierung der Seitenzahl fuer Deckblatt */
                anz_seiten-=auftrag.getSeitenzahl(AuftragsIDs_DB[m]);
                job.Seitenzahl=anz_seiten;
                /*Aktualisierung der Dateianzahl fuer Deckblatt */
                anz_dateien-=1;
                /*Aktualisierung der Seitenzahl in DB */
                job.updateSeitenanzahl();
                /*Aktualisierung des Preises nach neuer Seitenzahl in DB */
                job.updatePreis(preis);
                System.out.println("neue Seitenzahl: "+anz_seiten);
                System.out.println("neue Dateianzahl: "+anz_dateien);
                                
                /*Fehlermail senden */
                String error_file = auftrag.getDateiname(AuftragsIDs_DB[m]);
                this.sendErrorMail(error_file, sNr_job);

                /*Fehlerhaften Auftrag loeschen*/
                if(auftrag.deleteElement(AuftragsIDs_DB[m])!=0)
                        System.out.println("-----------------fehlerhafter Auftrag "+AuftragsIDs_DB[m]+ " - konnte nicht geloescht werden--------------");

                else System.out.println("-----------------Auftrag geloescht: "+AuftragsIDs_DB[m]+ " - keine passende Datei gefunden---------------");
                                
            }
            vorhanden = false;
        }

            /*bereinigte Dateien holen*/
            String[] filesToCombine = this.getFiles(sNr_job);
            /*Keine Auftraege mehr vorhanden - Bearbeitung beenden*/
            if(filesToCombine.length==0) return -1;
                    
            /** 
            * Ausfuehrung generate_barcode mit Pfad und Parameter
            * Input: parameter_barcode = { preis_ges, sNr}
            * Output: barcode_sNr.pdf
            */
            parameter_barcode = new String [] {
                String.valueOf(df.format(preis*anz_seiten)),
                sNr_job
            };
                    
            /*Uebergabe an runScript() des ProcessHandlers */
            endProcess.runScript(endProcess.generateBarcode, parameter_barcode);
                    
            /*Fehlermail senden bei Fehlerhafter Verarbeitung */
            if((ProcessHandler.exitValue)!=0){
                this.sendErrorMail(filesToCombine,sNr_job);
                return -1;
            }
                    
            /** 
            * Ausfuehrung generate_deckblatt mit Pfad und Parameter
            * Input :paramter_deckblatt = {sNr, number of files, number of pages, price}
            * Output: deckblatt_sNr.pdf
            */
            parameter_deckblatt= new String[] {
                sNr_job,
                String.valueOf(anz_dateien), 
                String.valueOf(anz_seiten), 
                String.valueOf(df.format(preis*anz_seiten))
            };
                    
            /*Uebergabe an runScript() des ProcessHandlers */
            endProcess.runScript(endProcess.deckblattErstellen, parameter_deckblatt);
                    
            /*Fehlermail senden bei Fehlerhafter Verarbeitung */
            if(ProcessHandler.exitValue!=0){
                this.sendErrorMail(filesToCombine,sNr_job);
                return -1;
            }

            /**Deckblatt mit leerer Seite zusammenfuegen ueber Skript combine*/
            parameter_combine = new String[] {
                sNr_job,
                path_preprocessed + "deckblatt_" + sNr_job +".pdf",                   
                "/home/druckauftrag/src/pdf_handling/final/leeres_pdf.pdf",
                "empty_pdf"
            };

            /*Uebergabe an runScript() des ProcessHandlers */
            endProcess.runScript(endProcess.combine, parameter_combine);

            /*Fehlermail senden bei Fehlerhafter Verarbeitung */
            if(ProcessHandler.exitValue!=0){
                this.sendErrorMail(filesToCombine,sNr_job);
                return -1;
            }
                    
            /**
             * Ausfuehrung Skript combine zur Zusammenfassung der Druckauftraege je Student.   
            * 1. Durchlauf Shellskript - Input :{sNr, deckblatt_sNr.pdf, pdf1}.
            * 2.-n. Durchlauf - Input :{Auftraggeber_Bibliotheksnummer, combined_sNr.pdf, pdf[2-n]}.
             * Output: combined_sNr.
            */
            parameter_combine = new String[] {
                sNr_job,
                path_preprocessed + "deckblatt_" + sNr_job +".pdf",
                path_preprocessed + filesToCombine[0]
            };
                    
            /*Uebergabe an runScript() des ProcessHandlers */
            endProcess.runScript(endProcess.combine, parameter_combine);

            /*Fehlermail senden bei Fehlerhafter Verarbeitung */
            if(ProcessHandler.exitValue!=0){
                 this.sendErrorMail(filesToCombine,sNr_job);
                return -1;
            }
                    
            /*Outputpdf der ersten Zusammenfassung als erste Datei in combine uebergeben */
            parameter_combine[1]=path_preprocessed+"combined_"+sNr_job+".pdf";
                    
            for(int i=1;i<filesToCombine.length;i++){

                /*naechste zugehoerige Datei in Ordner als zweite Datei in combine uebergeben */
                parameter_combine[2]=path_preprocessed + filesToCombine[i];

                /*Uebergabe an runScript() des ProcessHandlers */
                endProcess.runScript(endProcess.combine, parameter_combine);
                        
                /*Fehlermail senden bei Fehlerhafter Verarbeitung */
                if(ProcessHandler.exitValue!=0){
                    this.sendErrorMail(filesToCombine,sNr_job);
                    return -1;
                }
            }
                    
            /** Finales Dokument in Order "job" verschieben*/
            File file = new File(path_preprocessed + "/combined_"+ sNr_job +".pdf");
            file.renameTo(new File("/home/druckauftrag/Dateien/jobs/"+ job.Job_ID +".pdf"));

            System.out.println("");
            System.out.println("Druckjob: "+job.Job_ID+ " von Bib.Nr.: "+sNr_job+" erfolgreich verarbeitet");
            System.out.println("---------------------------------------------------------------------------------");
    
    }catch(IOException e){ 
        e.printStackTrace(); 
        return -1;
        }
    return 0;

    }


    /** 
     * Finden von Dateien mit passendem Praefix
     * @param searchpattern Dateipraefix nach dem gesucht werden soll (s-Nr Student).
     * @return Array mit gefundenen Dateinamen.
     * @throws IOException
     */
    public String[] getFiles(String searchpattern) throws IOException {

        /**Dateipfad zur Suche definieren*/
        String path = "/home/druckauftrag/Dateien/preprocessed";
        File directory = new File(path);

        /** Aufruf Bibliotheksmethode list (Klasse File) zur Erzeugung einer FileFilter Instanz.
         * Ueberschreibung Methode accept von FileFilter zur Suche nach Dateipraefix (startsWith).
         */
        String[] myFiles = directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(searchpattern);
            }
        });

        return myFiles;
    }

    /** 
     * Versender der Mail an Student bei Fehlerfall
     * @param error_files Namen der Dateien eines Druckjobs, welche nicht verarbeitet werden koennen
     * @param error_file Name der Datei, welche nicht verarbeitet werden kann
     */
    void sendErrorMail(String [] error_files, String sNr_empfaenger){

        System.out.println("---------------Senden Fehlermail an Bib.Nr.: "+sNr_empfaenger+"-------------------"); 
        //MailClient(String mailType, String recipient, String error,file )
        MailClient mailClient  = new MailClient("error", sNr_empfaenger + "@htw-dresden.de", "Kein Zusammenfassen der Druckauftraege moeglich ", Arrays.toString(error_files));
        mailClient.sendMail();
    }

    void sendErrorMail(String error_file, String sNr_empfaenger){

        System.out.println("---------------Senden Fehlermail an Bib.Nr.: "+sNr_empfaenger+"-------------------"); 
        //MailClient(String mailType, String recipient, String error,file )
        MailClient mailClient  = new MailClient("error", sNr_empfaenger + "@htw-dresden.de", "Keine Verarbeitung des Druckauftrages moeglich", error_file);
        mailClient.sendMail();
    }

}
