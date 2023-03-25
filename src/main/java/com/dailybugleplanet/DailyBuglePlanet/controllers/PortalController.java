/*
// Curso Egg FullStack
 */
package com.dailybugleplanet.DailyBuglePlanet.controllers;

// @author Ramiro Aybar
import com.dailybugleplanet.DailyBuglePlanet.entities.Account;
import com.dailybugleplanet.DailyBuglePlanet.enums.Roles;
import com.dailybugleplanet.DailyBuglePlanet.exceptions.NewsException;
import com.dailybugleplanet.DailyBuglePlanet.services.AccountService;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/")
public class PortalController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/signIn")
    public String signInForm(@RequestParam(required = false) String error, ModelMap model) {
        if (null != error) {
            model.put("error", "Usuario o contraseña incorrecta.");
        }
        model.put("action", "signIn");
        model.put("roles", Roles.values());
        return "index.html";
    }

    @GetMapping("/userprofile")
    public String profile() {
        return "profile.html";
    }

    @GetMapping("/signOff")
    public String signOff() {
        return "signOff.html";
    }

    @GetMapping("/signUp")
    public String signUpForm(ModelMap model) {
        model.put("roles", Roles.values());
        model.put("action", "signUp");
        return "signIn.html";
    }

    @GetMapping("/newsPortal")
    public String news() {
        return "news.html";
    }

    @GetMapping("/post")
    public String post() {
        return "post.html";
    }

    @GetMapping("/journalists")
    public String journalists() {
        return "journalists.html";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact.html";
    }

    @PostMapping("/signup")
    public String signUpUser(
            @RequestParam String name,
            @RequestParam String password,
            @RequestParam String confirm,
            @RequestParam Roles role,
            @RequestParam MultipartFile photo,
            ModelMap model) {

        try {
            accountService.signup(name, password, confirm, role, photo);
            model.put("name", name);
            model.put("action", "signIn");
        } catch (NewsException ne) {
            model.put("error", ne.getMessage());
            model.put("name", name);
            model.put("action", "signUp");
        } finally {
            model.put("roles", Roles.values());
        }
        return "signIn.html";
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        Account user = (Account) session.getAttribute("userSession");
        return "index.html";
    }

    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN','ROLE_JOURNALIST')")
    @GetMapping("/profile")
    public String profile(ModelMap model, HttpSession session) {
        Account user = (Account) session.getAttribute("userSession");
        model.put("user", user);
        return "profile-modify";
    }

    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN','ROLE_JOURNALIST')")
    @PostMapping("/profile/{id}")
    public String update(MultipartFile photo, @PathVariable String id,
            @RequestParam String name, @RequestParam String password,
            @RequestParam String confirm,
            ModelMap model, HttpSession session) {

        Account user = (Account) session.getAttribute("userSession");
        try {
            accountService.update(id, name, password, confirm, photo);
            if (user.getAccountType() == Roles.USER) {
                return "index";
            }
            model.put("journalists", accountService.getJournalistsAndAdmins());
            return "journalist-table";
        } catch (NewsException ne) {
            model.put("user", user);
            model.put("error", ne.getMessage());
            return "profile-modify";
        }
    }

}
