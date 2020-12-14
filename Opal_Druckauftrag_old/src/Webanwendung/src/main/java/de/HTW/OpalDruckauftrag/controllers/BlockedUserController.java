package de.HTW.OpalDruckauftrag.controllers;


import de.HTW.OpalDruckauftrag.Services.MailService;
import de.HTW.OpalDruckauftrag.entities.opal.Benutzer;
import de.HTW.OpalDruckauftrag.repositories.opal.BenutzerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.Collection;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Beantwortet benötigte HTTP-Requests zu gesperrten Nutzern (anzeigen, entsperren)
 * @author Francesco Ryplewitz, Felix Müller
 * @version 1.0
 */
@Controller
public class BlockedUserController {

    @Autowired
    private MailService mailService;

    private final BenutzerRepository userrepo;

    private Logger log;
    private Handler handler;

    /**
     * Konstruktor
     * @param userrepo Repository zugehöriger SQL-Statements
     */
    @Autowired
    public BlockedUserController(BenutzerRepository userrepo) throws IOException {
        this.userrepo = userrepo;

        log = Logger.getLogger(BlockedUserController.class.getName());
        handler = new FileHandler("blocked.txt");
        log.addHandler(handler);
        log.info("BlockedUserController initialisiert");
    }

    /**
     * GET-Request: DB-Abfragen nach gesperrten Nutzern, Eintragung als Attribut in Model
     * @param model Spring Model, benutzt von Thymeleaf
     * @return HTML-Seite (gelinkt als String)
     */
    @RequestMapping(path = "/blocked",  method = RequestMethod.GET)
    public String getBlockedUser (Model model){
        log.info("/blocked GET aufgerufen (blockierte Nutzer werden angezeigt)");
        Collection<Benutzer> userCollection = userrepo.findBlockedUser();
        model.addAttribute("users", userCollection);
        return "blockedUser";
    }

    /**
     * POST-Request: DB-Abfrage gesperrter Nutzer mit Suchparameter SNummer
     * @param Matrikel Suchparameter
     * @param model Spring Model, benutzt von Thymeleaf
     * @return HTML-Seite (gelinkt als String)
     */
    @PostMapping("/blocked")
    public String getBlockedUserByID(@RequestParam("Matrikel") String Matrikel, Model model){
        log.info("/blocked POST aufgerufen (SNummer: " + Matrikel + ")");
        Collection<Benutzer> jobCollection= userrepo.findBlockedUserByID(Matrikel);
        model.addAttribute("users",jobCollection);
        return "blockedUser";
    }

    /**
     * POST-Request: entsperren aller in HTML-Form gecheckter Nutzer:
     * - DB Update
     * - E-Mail versenden
     * Wenn kein Benutzer ausgewählt Umleitung zu /blocked (GET)
     * @param Matrikel Bearbeitungsparameter
     * @param model Spring Model, benutzt von Thymeleaf
     * @return HTML-Seite (gelinkt als String)
     */
    @PostMapping("/blocked/unblock")
    public String unBlockUser(@RequestParam(required=false, defaultValue = "0", name="Matrikel") String[] Matrikel, Model model){

        log.info("/blocked/unblock POST aufgerufen");

        if(Matrikel[0].equals("0")){
            log.info("Kein Benutzer ausgewählt");
            return "redirect:/blocked";
        }else {
            log.info("folgende Nutzer werden entsperrt und erhalten eine entsprechende E-Mail");
            log.info(String.valueOf(Matrikel));

            for(int i = 0; i < Matrikel.length; i++){
                System.out.println(Matrikel[i]);
                userrepo.unblockBenutzer(Matrikel[i]);
                log.info(Matrikel[i] + " wurde entsperrt");
                mailService.sendUnblockNotification(Matrikel[i]);
            }
            return "redirect:/blocked";
        }
    }
}