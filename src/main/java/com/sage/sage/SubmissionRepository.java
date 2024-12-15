package com.sage.sage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;  // Import the Optional class

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    // Retrieve all submissions for a specific assignment
    List<Submission> findByAssignment(Assignment assignment);

    // Retrieve all submissions by assignment and student
    List<Submission> findByAssignmentAndStudent(Assignment assignment, User student);

    // Method to find a submission by its content hash
    Optional<Submission> findByHash(String hash);

    // Method to check if a submission with the given hash exists
    boolean existsByHash(String hash);

    // Find all rejected submissions for a specific student
    List<Submission> findByStudentAndRejectedTrue(User student);

    // Find all rejected submissions for a specific assignment
    List<Submission> findByAssignmentAndRejectedTrue(Assignment assignment);

}