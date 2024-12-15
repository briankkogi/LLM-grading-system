## System Architecture Overview
The LLM-based Grading System is structured with a three-tier architecture to optimize separation of concerns and enhance system scalability. It also integrates Tesseract OCR for image detection to extract text from images:

- **Front-end**: Utilizes Thymeleaf for server-side rendering, optimizing interaction with HTML views.
- **Back-end**: Built on Spring Boot, managing all business logic, data processing, and server-side operations efficiently.
- **Database**: Uses MySQL, providing robust data storage capabilities and support for complex queries.
- **Image Detection**: Integrates Tesseract OCR for extracting text from images, enhancing the system's ability to process and analyze image-based submissions.

## Environment Setup
- **Java 21**: Required for leveraging advanced features and ensuring compatibility with the latest security standards.
- **IntelliJ IDEA**: Recommended IDE for its robust Java support, integrated debugging tools, and comprehensive Gradle integration.
- **MySQL Database**: Essential for data management; configure as follows in application properties:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/sage_db
    spring.datasource.username=root
    spring.datasource.password=[YourPassword]
    spring.jpa.hibernate.ddl-auto=none
    ```

## Testing Framework
- **JUnit**: Implement unit tests for all new features and bug fixes. Run tests via IntelliJ or Gradle to ensure functionality and prevent regressions.

## Image Detection
- **Tesseract OCR**: The project utilizes Tesseract for Optical Character Recognition (OCR) to extract text from images. Ensure Tesseract is installed and properly configured on your system. Update the `tessdata` path in the code if necessary.

## Troubleshooting and SQL Commands
### Common SQL Issue
Developers might encounter an issue where the size of the text content in submissions exceeds the default storage capacity of the content column in the submission table.

#### Problem Description
When submissions include large texts or documents, the default TEXT type column may not suffice, leading to data truncation errors.

#### Resolution Steps
1. **Identify the Issue**: If errors related to data truncation in the submission table’s content column are reported in your logs or error messages, consider increasing the capacity of the column.
2. **Modify the Database Schema**:
     - Open your MySQL database management tool or command line interface.
     - Ensure you are connected to the correct database where the submission table resides.
     - Run the following SQL command to alter the table and modify the content column:
         ```sql
         ALTER TABLE submission MODIFY content LONGTEXT;
         ```
3. **Verify the Change**:
     - After running the SQL command, execute a query to inspect the column definition:
         ```sql
         DESCRIBE submission;
         ```
4. **Test the Solution**:
     - Attempt to submit a larger text entry or document to confirm that the issue has been resolved and no data truncation occurs.

# User Manual

## Logging In and Accessing Features
### Login Process
1. Once the program is running, visit [http://localhost:8080/login](http://localhost:8080/login).
2. Create a new account (if you are a new user).
3. Enter your username and password.
4. Navigate through the dashboard to access various features such as Assignments, Grades, etc.

## Students
### Viewing Assignments
1. **Login**: Access the system by logging in with your student credentials.
2. **Navigate**: Click on “Assignments” in the main dashboard menu to view all available and past assignments.
3. **Review Details**: Each assignment entry includes the title, description, due date, and submission status. Click on any assignment to see more detailed information.

### Submitting Work
1. **Select an Assignment**: From the list of available assignments, click on the assignment you wish to complete.
2. **Upload Your Work**: Click “Choose File” to select your work file from your computer.
3. **Add Notes**: In the provided text area, add any relevant notes or comments for the instructor regarding your submission.
4. **Submit**: Review all information and click the “Submit” button to finalize your submission.

### Viewing Submissions
1. **Access Submissions**: Click on the “Submissions” tab in the navigation menu to view a list of all your submitted assignments.
2. **Review Submission Details**: Each listed submission will include the assignment title, submission date, and current status (e.g., Pending Review, Graded). Click on a submission to open and view detailed submission files, any notes you added, and feedback from the instructor if available.

### Viewing Grades
1. **Review Grades**: The grades page will display a list of assignments along with your score, feedback comments, and the date graded. This allows you to track your academic performance over the course of the semester.
2. **Detailed Feedback**: For detailed feedback, click on the view feedback on the table. This will provide you with specific comments, potential areas for improvement (if applicable).

## Lecturers
### Creating Assignments
1. **Login and Navigate**: After logging in, navigate to the “Assignments” tab on the main dashboard.
2. **Create New Assignment**:
     - Click on “Create New Assignment” to open the assignment creation form.
     - Enter Assignment Details: Fill in the form with the assignment title, description, due date.
3. **Publish Assignment**:
     - Review all the details to ensure accuracy.
     - Click on “Create Assignment” to make the assignment available to students.

### Reviewing AI-Graded Submissions
1. **Access Submissions**:
     - Navigate to the “View Submissions” where you can see a list of all submissions that have been automatically graded by the AI system.
     - Each submission entry includes the student’s name, submission date, and the AI-generated grade.
2. **Review AI Grades**:
     - Click on a submission to review the detailed work along with the AI-generated grade and feedback.
     - Evaluate the AI grading to ensure it aligns with academic standards and expectations.

### Rejecting Submissions
1. **Identify Issues**: During your review, if a submission fails to meet the assignment criteria or has issues that the AI did not appropriately flag, initiate a rejection.
2. **Provide Reason for Rejection**:
     - Select “Reject Submission”, and a prompt will appear for you to enter a reason for the rejection.
     - Clearly state why the submission is rejected, such as “Plagiarism detected”, “Off-topic submission”, or “Incomplete analysis”.
3. **Notify Student**:
     - Submit the rejection with your comments. The student will receive a notification along with the reasons for rejection, providing them an opportunity to understand the deficiencies and make necessary corrections.
