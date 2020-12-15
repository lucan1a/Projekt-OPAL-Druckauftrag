package de.HTW.OpalDruckauftrag.controllers;


import de.HTW.OpalDruckauftrag.repositories.user.UserReporitory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Beantwortet HTTP-Requests für den Login
 * @author Francesco Ryplewitz
 * @version 1.2
 */
@Controller
public class LoginController {

    private final UserReporitory userrepo;

    private Logger log;
    private Handler handler;
    /**
     * Konstruktor
     * @param userrepo Repository zugehöriger SQl-Statements
     */
    public LoginController(UserReporitory userrepo) throws IOException {
        log = Logger.getLogger(LoginController.class.getName());
        handler = new FileHandler("log.txt");
        log.addHandler(handler);
        log.log(Level.INFO, "LoginController initialisiert");
        this.userrepo = userrepo;

    }

    /**
     * Beantwortet alle Requests nach "/login"
     * @param model Spring Model
     * @return HTML-Seite (gelinkt als String)
     */
    @RequestMapping(path = "/login")
    public String login(Model model) {

        log.log(Level.INFO, "/login aufgerufen");
        return "login";
    }

    @RequestMapping(path="/ertzu")
    public String changesite(Model model){
        log.log(Level.WARNING, "/ertzu aufgerufen");
        return "ertzu";
    }

    @RequestMapping(path="/ertzu",method = RequestMethod.POST)
    public String change(@RequestParam("password") String Password,Model model){
        String encoded = new BCryptPasswordEncoder().encode(Password);
        userrepo.ChangePassword(encoded);
        log.log(Level.INFO, "Passwort geändert");
        return "redirect:/login";
    }
}
