package com.sage.sage.controller;

import com.sage.sage.UserRepository;
import com.sage.sage.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, Model model, HttpSession session) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user); // Store user in session after successful login

            // Redirect based on the user role
            if ("student".equals(user.getRole())) {
                return "redirect:/student/dashboard"; // Redirect to student dashboard
            } else if ("lecturer".equals(user.getRole())) {
                return "redirect:/lecturer/dashboard"; // Redirect to lecturer dashboard
            } else {
                // If role is not defined or recognized, redirect to a generic home page or error page
                return "redirect:/home";
            }
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login"; // Stay on the login page and show an error
        }
    }
}