package de.HTW.OpalDruckauftrag.controllers;

import de.HTW.OpalDruckauftrag.entities.opal.Job;
import de.HTW.OpalDruckauftrag.repositories.opal.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



/**
 * Controller: Beantwortet HTTP-Request zum Download und Komprimieren der Dokumente und zum Bestätigen den Downloads
 * @author Felix Müller
 * @version 1.1
 */
@Controller
public class FileDownloadController {

    private final JobRepository jobRepo;

    private List<Job> downloadedJobs;
    private List<Job> failedJobs;

    private final String docExtension = ".pdf";
    private final String zipExtension = ".zip";

    private final String folderPath = "/home/druckauftrag/Dateien/jobs/";
    private final String zipDir = "Druckjobs_zip";
    private final String zipFile = zipDir + zipExtension;

    private Logger log;
    private Handler handler;

    /**
     * Konstruktor
     * initialisiert Liste der heruntergeladenen und fehlgeschlagenen Jobs als LinkedList (aufgrund der vielen Daten performanter)
     * @param jobRepo Repository zugehöriger SQL-Statements
     */
    @Autowired
    public FileDownloadController(JobRepository jobRepo) throws IOException {
        this.jobRepo    = jobRepo;
        downloadedJobs  = new LinkedList<>();
        failedJobs      = new LinkedList<>();

        log = Logger.getLogger(FileDownloadController.class.getName() );
        handler = new FileHandler("download.txt");
        log.addHandler(handler);
        log.info("FileDownloadController initialisiert");
    }

    /**
     * Umleitung zu Bestätigungsseite nach Download
     * @return HTML-Seite (gelinkt als String)
     */
    @RequestMapping (path = "/download/confirm")
    public String downloadConfirm() {
        log.info("Bestätigungsseite für Download angezeigt");
        return "downloadConfirm";
    }

    /**
     * ClearUp(), Aktualisiert DB und löscht Dokumente, danach Umleitung zu Jobs
     * @return HTML-Seite (gelinkt als String)
     */
    @RequestMapping (path = "/download/ok")
    public String downloadOk() {
        log.info("Download wurde bestätigt");
        clearUp();
        for(Job job : downloadedJobs){
            if(!failedJobs.contains(job)){
                jobRepo.updateByID(String.valueOf(job.getId()));
                log.info("Job " + job.getId() + " Druckstatus gesetzt (DB)");
                if(!deleteFile(job)){
                    log.log(Level.SEVERE, "Job " + job.getId() + " konnte nicht gelöscht werden");
                }
            }else{
                log.log(Level.WARNING, "Job " + job.getId() + " ist als fehlgeschlagen markiert, versuche nciht zu löschen");
            }
        }
        failedJobs.clear();
        downloadedJobs.clear();
        return "redirect:/jobs";
    }

    /**
     * ClearUp() und Umleitung nach /jobs
     * @return HTML-Seite (gelinkt als String)
     */
    @RequestMapping(path = "/download/no")
    public String downloadNo(){
        log.log(Level.WARNING, "Download wurde als fehlerhaft gekennzeichnet");
        clearUp();
        downloadedJobs.clear();
        for(Job job : failedJobs){
            log.log(Level.WARNING, job.getId() + " war zudem als fehlgeschlagen markiert");
        }
        return "redirect:/jobs";
    }

    /**
     * löscht ein zu einem Druckjob gehörendes Dokument
     * @param job Löschparameter
     * @return FALSE wenn Datei nicht vorhanden oder bei IOException, sonst TRUE
     */
    private boolean deleteFile(Job job) {
        String job_id = String.valueOf(job.getId());
        Path filePath = Paths.get(folderPath + job_id + docExtension);
        Path zipPath = Paths.get(folderPath + job_id + docExtension);
        if (Files.exists(filePath)) {
            /*Dokument zu Job_ID ist vorhanden, versuche es zu löschen*/
            try {
                log.log(Level.INFO, "versuche " + filePath.toString() + " zu löschen");
                Files.delete(filePath);
                return true;
            } catch (IOException ioe) {
                log.log(Level.SEVERE, "Löschen von " + filePath.toString() + " führt zu IOException");
                return false;
            }
        } else {
            log.log(Level.SEVERE, filePath.toString() + " ist nicht vorhanden");
            return false;
        }
    }

    /**
     * Erzeugt ein neues Verzeichnis (Druckjobs_zip) für die zum Download angeforderten Dokumente
     * @return TRUE wenn Verzeichnis erstellt ist, FALSE wenn Verzeichnis nicht erstellt werden konnte
     */
    private boolean createZipDirectory() {
        Path path = Paths.get(folderPath + zipDir);
        System.out.println(path);
        try {
            Files.createDirectory(path);
            log.log(Level.INFO, "Zip-Verzeichnis erzeugt");
            return true;
        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Zip-Verzeichnis konnte nicht erzeugt werden");
            return false;
        }
    }

    /**
     * einzelnes Dokument wird in das neue Verzeichnis (Druckjobs_zip) kopiert
     * @param job aus DB abgefragter Job
     * @return TRUE move hat funktioniert, FALSE IOEception oder Dokument nicht vorhanden
     */
    private boolean copyFile(Job job) {
        String job_id = String.valueOf(job.getId());
        String job_benutzer = job.getAuftraggeber_Bibliotheksnummer();
        //Quelle im Job Verzeichnis
        Path filePath = Paths.get(folderPath + job_id + docExtension);
        //Ziel, Dateiname: sNummer_JobID.pdf
        Path zipPath = Paths.get(folderPath + zipDir + "/" + job_benutzer + "_" + job_id + docExtension);
        if (Files.exists(filePath)) {
            /*Dokument zu Job_ID ist vorhanden, versuche es in Zip-Verzeichnis zu kopieren*/
            try {
                log.log(Level.INFO, filePath + " nach " + zipPath + " kopieren");
                Files.copy(filePath, zipPath);
                return true;
            } catch (IOException ioe) {
                log.log(Level.SEVERE, "Kopieren führt zu IOException");
                return false;
            }
        } else {
            /*Dokument zu Job_ID existiert nicht. Frag dich mal warum!*/
            return false;
        }
    }

    /**
     * Komprimiert alle Dokumente im Verzeichnis Druckjobs_zip
     * nutzt FOS, FIS, ZipOS und ZipEntrys ->IOException wird gecatcht
     * @return TRUE Erfolg, FALSE Misserfolg (IOException in Funktion zipFile())
     */
    private boolean doZip() {
        try {
            String sourceFile = folderPath + zipDir;
            FileOutputStream fos = new FileOutputStream(folderPath + zipDir + zipExtension);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(sourceFile);

            compFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();

            log.log(Level.INFO, "Zip-Datei erfolgreich erstellt");
            return true;
        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Erzeugen der Zip-Datei führt zu IOException");
            return false;
        }
    }

    /**
     * rekursiv aufgerufene Funktion zum Komprimieren von Verzeichnisen
     * @param fileToZip zu komprimierendes Verzeichnes
     * @param fileName  Verzeichnisname, Dateiname
     * @param zipOut    ZipOutputStream
     * @throws IOException durch Dateiarbeit und Streams wird in aufrufender Funktion doZip() behandelt
     */
    private void compFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            }
            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                compFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    /**
     * entfernt Zip-Verzeichnis und Zip-Datei
     */
    private void clearUp() {
        File zipDirectory = new File(folderPath + zipDir);
        File file = new File(folderPath + zipFile);

        if (zipDirectory.exists()) {
            String[]entries = zipDirectory.list();
            for(String s: entries){
                File currentFile = new File(zipDirectory.getPath(),s);
                currentFile.delete();
            }
            zipDirectory.delete();
        }
        if (file.exists()) {
            file.delete();
        }
        log.log(Level.INFO, "Zip-Verzeichnis und Zip-Datei entfernt");
    }

    /**
     * GET-Request: Download aller verfügbaren Jobs(DB-Abfrage nach Druckjobs mit Druckstatus 0)
     * nutzt createZipDirectory, moveFile, doZip, zipFile, um alle Dokumente in Druckjobs_zip zu komprimieren
     * enstandene Datei wird ResponesEntity übergeben, HTPP-Header mit Content-Disposition, no-cache, Datei-name und Typ gesetzt
     * im Fehlerfall wird ein HTTP-Header mit Status 500 übergeben
     * @return ResponseEntity mit HTTP-Header und FileStream, NULL bei Fehler
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity download() {
        log.info( "Download angefordert" );
        clearUp();

        /*alle Druckjobs mit Druckstatus==0 (noch nicht heruntergeladen) finden*/
        downloadedJobs = (List<Job>) jobRepo.findAllUnprinted();
        int numJobs = downloadedJobs.size();

        log.info( numJobs + " Druckjobs in Datenbank gefunden" );

        /*Hier wird fröhlich komprimiert*/
        if (createZipDirectory()) {
            /*alles gut*/
            for (Job job : downloadedJobs) {
                if (!copyFile(job)) {
                    log.log( Level.WARNING, job.getId() + " konnte nicht in Zip-Verzeichnis kopiert werden" );
                }
            }

            doZip();

            try {
                String filePath = folderPath + zipFile;
                File file = new File(filePath);
                FileInputStream fis = new FileInputStream(file);

                InputStreamResource ressource = new InputStreamResource(fis);

                HttpHeaders headerSuccess = new HttpHeaders();
                headerSuccess.add("Content-Disposition", String.format("attachment; filename=\"%s", zipFile));
                headerSuccess.add("Cache-Control", "no-cache, no-store, must-revalidate");
                headerSuccess.add("Pragma", "no-cache");
                headerSuccess.add("Expires", "0");

                log.info("Download serverseitig ok");
                return ResponseEntity.status(200).headers(headerSuccess).
                        contentLength(file.length()).contentType(MediaType.parseMediaType("appilcation/zip")).body(ressource);

            }catch(FileNotFoundException e){
                log.log(Level.SEVERE, "FileNotFoundException Zip-Datei");
                HttpHeaders headerFail1 = new HttpHeaders();
                headerFail1.add("Cache-Control", "no-cache, no-store, must-revalidate");
                return ResponseEntity.status(500).headers(headerFail1).body(null);
            }

        } else {
            log.log(Level.SEVERE, "konnte Zip-Verzeichnisstruktur nicht erstellen");
            HttpHeaders headerFail2 = new HttpHeaders();
            headerFail2.add("Cache-Control", "no-cache, no-store, must-revalidate");
            return ResponseEntity.status(500).headers(headerFail2).body(null);
        }
    }
}