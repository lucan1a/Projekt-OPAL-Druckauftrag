package de.HTW.OpalDruckauftrag.controllers;

import de.HTW.OpalDruckauftrag.Services.MailService;
import de.HTW.OpalDruckauftrag.entities.opal.Job;
import de.HTW.OpalDruckauftrag.repositories.opal.JobRepository;
import de.HTW.OpalDruckauftrag.repositories.opal.BenutzerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Beantwortet benötigte HTTP-Requests zu zeitkritischen Druckjobs (Anzeigen, Benutzer sperren)
 * @author Francesco Ryplewitz
 * @version 1.0
 */
@Controller
public class CriticalJobsController {

    private final JobRepository jobRepo;
    private final BenutzerRepository userrrepo;

    private Logger log;
    private Handler handler;

    @Autowired
    private MailService mailService;

    /**
     * Konstruktor
     * @param jobRepo Repository zugehöriger SQL-Statements (Druckjobs)
     * @param userrrepo Repository zugehöriger SQL-Statements (Benutzer)
     */
    public CriticalJobsController(JobRepository jobRepo, BenutzerRepository userrrepo) throws IOException {
        this.jobRepo = jobRepo;
        this.userrrepo = userrrepo;

        log = Logger.getLogger(CriticalJobsController.class.getName());
        handler = new FileHandler("critical.txt");
        log.addHandler(handler);
        log.info("CriticalJobsController initialisiert");
    }

    /**
     * GET-Request: DB-Abfrage nach zeitkritischen Druckjobs, Eintragung in Model
     * @param model Spring Model, benutzt von Thymeleaf
     * @return HTML-Seite (gelinkt als String)
     */
    @RequestMapping(path = "/critical", method = RequestMethod.GET)
    public String getCriticalJobs(Model model) {
        Collection<Job> jobCollectionBlocked = jobRepo.findCriticalJobsBlocked();
        Collection<Job> jobCollectionNotBlocked = jobRepo.findCriticalJobsNotBlocked();
        model.addAttribute("jobs_blocked", jobCollectionBlocked);
        model.addAttribute("jobs_not_blocked", jobCollectionNotBlocked);
        log.info("/critical GET aufgerufen (zeitkritische Druckjobs werden angezeigt)");
        return "criticalJobs";
    }

    /**
     * POST-Request: Sperren aller in HTML-Form gecheckter Benutzer anschließend Umleitung auf /critical (GET):
     * - DB Update
     * - E-Mail versenden
     * Wenn Kein Benutzer ausgewählt ist Umleitung auf /critical (GET)
     * @param Matrikel Bearbeitungsparameter
     * @param model Spring Model, benutzt von Thymeleaf
     * @return HTML-Seite (gelinkt als String)
     */
    @PostMapping("/critical/block")
    public String blockUser(@RequestParam(required = false, defaultValue = "0" ,name = "Matrikel") String[] Matrikel, Model model) {

        log.info("/critical/block POST aufgerufen");

        if(Matrikel[0].equals("0")){
            log.info("Kein Benutzer ausgewählt");
            return "redirect:/critical";
        }else {
            log.info("folgende Benutzer werden gesperrt und erhalten eine entsprechende E-Mail");
            log.info(String.valueOf(Matrikel));
            for (int i = 0; i < Matrikel.length; i++) {
                System.out.println(Matrikel[i]);
                userrrepo.blockBenutzer(Matrikel[i]);
                log.info(Matrikel[i] + "Wurde bearbeitet!");
                mailService.sendBlockNotification(Matrikel[i]);
            }
            return "redirect:/critical";
        }
    }
}
