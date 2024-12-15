package com.sage.sage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpSession;
import com.sage.sage.User;
import com.sage.sage.Assignment;
import com.sage.sage.AssignmentRepository;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    // Mapping for the student dashboard
    @GetMapping("/student/dashboard")
    public ModelAndView studentDashboard(HttpSession session) {
        // Ensure that only students can access the student dashboard
        User user = (User) session.getAttribute("user");
        if (user != null && "student".equals(user.getRole())) {
            ModelAndView modelAndView = new ModelAndView("studentDashboard"); // Points to the Thymeleaf template
            modelAndView.addObject("username", user.getUsername()); // Add user details to the model

            // Fetch and add assignments to the model
            List<Assignment> assignments = assignmentRepository.findAll();
            modelAndView.addObject("assignments", assignments);

            return modelAndView;
        } else {
            // Redirect to login if the user is not authorized
            return new ModelAndView("redirect:/login");
        }
    }

    // Mapping for the lecturer dashboard
    @GetMapping("/lecturer/dashboard")
    public ModelAndView lecturerDashboard(HttpSession session) {
        // Ensure that only lecturers can access the lecturer dashboard
        User user = (User) session.getAttribute("user");
        if (user != null && "lecturer".equals(user.getRole())) {
            ModelAndView modelAndView = new ModelAndView("lecturerDashboard"); // Points to the Thymeleaf template
            modelAndView.addObject("username", user.getUsername()); // Add user details to the model

            // Fetch assignments created by the lecturer
            List<Assignment> assignments = assignmentRepository.findByCreatedBy(user);
            modelAndView.addObject("assignments", assignments);

            return modelAndView;
        } else {
            // Redirect to login if the user is not authorized
            return new ModelAndView("redirect:/login");
        }
    }
}