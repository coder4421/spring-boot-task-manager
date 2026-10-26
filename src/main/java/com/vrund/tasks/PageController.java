package com.vrund.tasks;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
class PageController {
    @GetMapping("/") String home(Principal user, Model model) { model.addAttribute("username", user.getName()); return "index"; }
    @GetMapping("/login") String login() { return "login"; }
}
