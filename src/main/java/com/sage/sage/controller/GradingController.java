package com.sage.sage.controller;

import com.sage.sage.*;
import com.sage.sage.services.CohereGradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GradingController {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private CohereGradingService cohereGradingService;

    // Lecturer: Grade a specific submission using AI
    @PostMapping("/lecturer/submissions/{submissionId}/grade")
    public String gradeSubmission(@PathVariable Long submissionId, Model model) {
        Submission submission = submissionRepository.findById(submissionId).orElse(null);

        if (submission == null) {
            model.addAttribute("error", "Submission not found.");
            return "redirect:/lecturer/assignments";
        }

        try {
            // Call Cohere API to get grading feedback
            CohereGradingService.GradingResult result = cohereGradingService.gradeSubmission(submission.getContent());

            // Save the grade and feedback in the database
            Grade grade = new Grade();
            grade.setSubmission(submission);
            grade.setGrade(result.getGrade());
            grade.setFeedback(result.getFeedback());

            gradeRepository.save(grade);

            model.addAttribute("success", "Submission graded successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error during grading: " + e.getMessage());
        }

        return "redirect:/lecturer/assignments/" + submission.getAssignment().getId() + "/submissions";
    }

    @PostMapping("/submissions/{id}/reject")
    public String rejectSubmission(@PathVariable Long id, String rejectionReason, Model model) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid submission ID: " + id));
        submission.setRejected(true);
        submission.setRejectionReason(rejectionReason);
        submissionRepository.save(submission);
        model.addAttribute("message", "Submission rejected successfully.");
        return "redirect:/lecturer/assignments/" + submission.getAssignment().getId() + "/submissions";
    }

}