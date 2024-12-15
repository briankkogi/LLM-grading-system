package com.sage.sage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/student")
    public ModelAndView studentRegistrationForm() {
        return new ModelAndView("registerStudent");
    }

    @GetMapping("/lecturer")
    public ModelAndView lecturerRegistrationForm() {
        return new ModelAndView("registerLecturer");
    }

    @PostMapping("/student")
    public ModelAndView registerStudent(@RequestParam String username, @RequestParam String password,
                                        @RequestParam String role, @RequestParam String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // Consider implementing password hashing here
        user.setRole("student");
        user.setEmail(email);
        userRepository.save(user);
        return new ModelAndView("redirect:/login");
    }

    @PostMapping("/lecturer")
    public ModelAndView registerLecturer(@RequestParam String username, @RequestParam String password,
                                         @RequestParam String role, @RequestParam String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // Consider implementing password hashing here
        user.setRole("lecturer");
        user.setEmail(email);
        userRepository.save(user);
        return new ModelAndView("redirect:/login");
    }
}