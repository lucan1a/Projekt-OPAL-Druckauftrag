package de.HTW.OpalDruckauftrag.controllers;

import de.HTW.OpalDruckauftrag.entities.opal.Job;
import de.HTW.OpalDruckauftrag.repositories.opal.BenutzerRepository;
import de.HTW.OpalDruckauftrag.repositories.opal.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Beantwortet benötigte HTTP-Requests zu Druckjobs (Anzeigen, Suche, Abholung bestätigt)
 * @author Francesco Ryplewitz, Felix Müller
 * @version 1.0
 */
@Controller
public class JobController {

    private final JobRepository jobRepo;
    private final BenutzerRepository benutzerrepo;

    private Logger log;
    private Handler handler;

    /**
     * Konstruktor
     * @param jobRepo Repository zugehöriger SQL-Statements
     * @param benutzerrepo
     */
    @Autowired
    public JobController(JobRepository jobRepo, BenutzerRepository benutzerrepo) throws IOException {
        this.jobRepo = jobRepo;
        this.benutzerrepo = benutzerrepo;
        log = Logger.getLogger(JobController.class.getName() );
        handler = new FileHandler("jobs.txt");
        log.addHandler(handler);
        log.info("JobController initialisiert");
    }

    /**
     * GET-Request: DB-Abfrage nach nicht gedruckten Druckjobs, Eintragung als Atrribut in Model
     * @param model Spring Model, benutzt von Thymeleaf
     * @return HTML-Seite (gelinkt als String)
     */
    @RequestMapping(path = "/jobs", method = RequestMethod.GET)
    public String getReadyJobs(Model model){
        log.info("/jobs GET aufgerufen (nicht gedruckte Jobs werden angezeigt)");
        Collection<Job> jobCollection =jobRepo.findAllPrinted();
        model.addAttribute("jobs", jobCollection);
        return "showJobs";
    }

    /**
     * POST-Request: Einen Druckjob als gedruckt markieren
     * @param Id Parameter
     * @param model Spring Model, benutzt von Thymeleaf
     * @return
     */
    @PostMapping("jobs/printed")
    public String setJobPrinted(@RequestParam("printed") String Id, Model model){

        log.info(Id + " wurde gedruckt");
        jobRepo.updatePrinted(Id);
        return "redirect:/jobs";
    }

    /**
     * POST-Request: DB-Abfrage nach Druckaufträgen eines Auftraggebers (SNummer)
     * Eintragung als Attribut in Model
     * @param Matrikel Suchparameter
     * @param model Spring Model, benutzt von Thymeleaf
     * @return HTML-Seite (gelinkt als String)
     */
    @PostMapping("/jobs")
    public String getJobsById(@RequestParam("Matrikel") String Matrikel, Model model){
        log.info("/jobs POST aufgerufen (SNummer: " + Matrikel + ")");
        Collection<Job> jobCollection= jobRepo.findByID(Matrikel);
        model.addAttribute("jobs",jobCollection);
        return "showJobs";
    }

    /**
     * POST-Request: Löschen aller in HTML-Form gecheckter Druckjobs und zugehörigen Benutzer aus DB anschließend Umleitung auf /jobs (GET)
     * Wenn Kein Druckjob ausgewählt ist Umleitung zu /jobs (GET)
     * @param Id Löschparameter
     * @param model Spring Model, benutzt von Thymeleaf
     * @return HTML-Seite (gelinkt als String)
     */
    @PostMapping("/jobs/pick")
    public String pickJobs(@RequestParam(required = false, defaultValue = "0", name = "id") String[] Id, Model model) {

        log.info("/jobs/pick POST aufgerufen");

        if(Id[0].equals("0")){
            log.info("Kein Druckjob ausgewählt");
            return "redirect:/jobs";
        }else {
            log.log(Level.INFO, "folgende Druckjobs werden gelöscht:");
            log.log(Level.INFO, String.valueOf(Id));

            for (String text : Id) {
                log.info(text + " gelöscht");
                String sNummer = jobRepo.getSnummerByJobID(Short.parseShort(text));
                jobRepo.deleteByID(text);
                benutzerrepo.deleteBenutzerByID(sNummer);
            }

            return "redirect:/jobs";
        }
    }
}