package com.sage.sage.controller;

import com.sage.sage.*;
import com.sage.sage.services.CohereGradingService;
import com.sage.sage.services.CohereGradingService.GradingResult;
import com.sage.sage.document.PDFProcessor;
import com.sage.sage.document.WordProcessor;
import com.sage.sage.document.OCRProcessor;
import com.sage.sage.util.HashGenerator;
import jakarta.servlet.http.HttpSession;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.security.NoSuchAlgorithmException;

@Controller
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final CohereGradingService cohereGradingService;

    private final PDFProcessor pdfProcessor = new PDFProcessor();
    private final WordProcessor wordProcessor = new WordProcessor();
    private final OCRProcessor ocrProcessor = new OCRProcessor();

    @Autowired
    public AssignmentController(
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            GradeRepository gradeRepository,
            CohereGradingService cohereGradingService) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.gradeRepository = gradeRepository;
        this.cohereGradingService = cohereGradingService;
    }

    private boolean isAuthorized(HttpSession session, String role) {
        User user = (User) session.getAttribute("user");
        return user != null && role.equals(user.getRole());
    }

    @GetMapping("/lecturer/create-assignment")
    public String showCreateAssignmentForm(HttpSession session, Model model) {
        if (!isAuthorized(session, "lecturer")) {
            return "redirect:/login";
        }
        model.addAttribute("assignment", new Assignment());
        return "createAssignment";
    }

    @PostMapping("/lecturer/create-assignment")
    public String createAssignment(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String dueDate,
            HttpSession session,
            Model model) {

        if (!isAuthorized(session, "lecturer")) {
            return "redirect:/login";
        }

        try {
            Assignment assignment = new Assignment();
            assignment.setTitle(title);
            assignment.setDescription(description);
            assignment.setDueDate(LocalDate.parse(dueDate));
            assignment.setCreatedBy((User) session.getAttribute("user"));

            assignmentRepository.save(assignment);

            model.addAttribute("success", "Assignment created successfully!");
            return "redirect:/lecturer/assignments";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create assignment: " + e.getMessage());
            return "createAssignment";
        }
    }

    @GetMapping("/student/assignments")
    public String viewAssignments(HttpSession session, Model model) {
        if (!isAuthorized(session, "student")) {
            return "redirect:/login";
        }

        List<Assignment> assignments = assignmentRepository.findAll();
        model.addAttribute("assignments", assignments);
        return "studentAssignments";
    }

    @GetMapping("/lecturer/assignments")
    public String viewLecturerAssignments(HttpSession session, Model model) {
        if (!isAuthorized(session, "lecturer")) {
            return "redirect:/login";
        }

        User lecturer = (User) session.getAttribute("user");
        List<Assignment> assignments = assignmentRepository.findByCreatedBy(lecturer);
        model.addAttribute("assignments", assignments);
        return "lecturerAssignments";
    }

    @GetMapping("/student/assignments/{assignmentId}")
    public String showSubmissionForm(@PathVariable Long assignmentId, HttpSession session, Model model) {
        if (!isAuthorized(session, "student")) {
            return "redirect:/login";
        }

        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) {
            model.addAttribute("error", "Assignment not found.");
            return "studentAssignments";
        }

        model.addAttribute("assignment", assignment);
        return "submitAssignment";
    }

    @PostMapping("/student/assignments/{assignmentId}/submit")
    public String submitAssignment(
            @PathVariable Long assignmentId,
            @RequestParam String content,
            @RequestParam("file") MultipartFile file,
            HttpSession session,
            Model model) throws IOException, TesseractException, NoSuchAlgorithmException {

        if (!isAuthorized(session, "student")) {
            return "redirect:/login";
        }

        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) {
            model.addAttribute("error", "Assignment not found.");
            return "studentAssignments";
        }

        String extractedText = "";
        if (!file.isEmpty()) {
            Path tempFile = Files.createTempFile("uploaded_", "_" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            String fileType = file.getContentType();
            if (fileType != null) {
                if (fileType.equals("application/pdf")) {
                    extractedText = pdfProcessor.extractTextFromPDF(tempFile.toString());
                } else if (fileType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                    extractedText = wordProcessor.extractTextFromWord(tempFile.toString());
                } else if (fileType.startsWith("image/")) {
                    extractedText = ocrProcessor.extractTextFromImage(tempFile.toString());
                }
            }

            Files.delete(tempFile);
        }

        String finalContent = content + (extractedText.isEmpty() ? "" : "\n\n[Uploaded Content:]\n" + extractedText);

        String contentHash = HashGenerator.generateHash(finalContent, "SHA-256");
        if (submissionRepository.existsByHash(contentHash)) {
            model.addAttribute("error", "Duplicate submission detected based on content hash.");
            return "submitAssignment";
        }

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent((User) session.getAttribute("user"));
        submission.setContent(finalContent);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setHash(contentHash);

        submissionRepository.save(submission);
        model.addAttribute("success", "Submission successful!");
        return "redirect:/student/dashboard";
    }

    @GetMapping("/lecturer/assignments/{assignmentId}/submissions")
    public String viewSubmissions(@PathVariable Long assignmentId, HttpSession session, Model model) {
        if (!isAuthorized(session, "lecturer")) {
            return "redirect:/login";
        }

        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) {
            model.addAttribute("error", "Assignment not found.");
            return "lecturerAssignments";
        }

        List<Submission> submissions = submissionRepository.findByAssignment(assignment);
        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", submissions);
        return "viewSubmissions";
    }

    @GetMapping("/student/assignments/{assignmentId}/submissions")
    public String viewStudentSubmissions(@PathVariable Long assignmentId, HttpSession session, Model model) {
        if (!isAuthorized(session, "student")) {
            return "redirect:/login";
        }

        User student = (User) session.getAttribute("user");
        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) {
            model.addAttribute("error", "Assignment not found.");
            return "studentAssignments";
        }

        List<Submission> submissions = submissionRepository.findByAssignmentAndStudent(assignment, student);
        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", submissions);

        return "studentSubmissions"; // Ensure you have a view named 'studentSubmissions.html'
    }

    @PostMapping("/lecturer/assignments/{assignmentId}/submissions/{submissionId}/grade")
    public String gradeSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            HttpSession session,
            Model model) {

        if (!isAuthorized(session, "lecturer")) {
            return "redirect:/login";
        }

        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            model.addAttribute("error", "Submission not found.");
            return "redirect:/lecturer/assignments/" + assignmentId + "/submissions";
        }

        try {
            GradingResult gradingResult = cohereGradingService.gradeSubmission(submission.getContent());

            Grade grade = new Grade();
            grade.setSubmission(submission);
            grade.setGrade(gradingResult.getGrade());
            grade.setFeedback(gradingResult.getFeedback());

            gradeRepository.save(grade);

            model.addAttribute("success", "Submission graded successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error while grading submission: " + e.getMessage());
        }

        return "redirect:/lecturer/assignments/" + assignmentId + "/submissions";
    }
}