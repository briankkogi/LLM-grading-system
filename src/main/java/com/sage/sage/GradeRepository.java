package com.sage.sage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    Grade findBySubmission(Submission submission); // Find grade by submission
}