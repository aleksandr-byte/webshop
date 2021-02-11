package ua.nure.webshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ua.nure.webshop.domain.Role;
import ua.nure.webshop.domain.User;
import ua.nure.webshop.repos.UserRepository;

import java.util.Collections;

@Controller
public class RegistrationController {

    private UserRepository userRepo;

    public RegistrationController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/registration")
    public String registration() {
        return "users/registration";
    }

    @PostMapping("/registration")
    public String addUser(User user, Model model) {
        User userFromDb = userRepo.findByUsername(user.getUsername());

        if (userFromDb != null) {
            model.addAttribute("message", "User exists!");
            return "users/registration";
        }

        user.setRoles(Collections.singleton(Role.USER));
        user.setActive(true);
        userRepo.save(user);

        return "redirect:/login";
    }
}
