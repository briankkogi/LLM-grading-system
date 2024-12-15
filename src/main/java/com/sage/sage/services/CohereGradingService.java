package com.sage.sage.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class CohereGradingService {

    @Value("${cohere.api.key}")
    private String apiKey; //API key in application.properties

    private static final String API_URL = "https://api.cohere.ai/v1/generate";
    private static final String MODEL = "command-xlarge-nightly";
    private static final double TEMPERATURE = 0.7;
    private static final int MAX_TOKENS = 300;

    public GradingResult gradeSubmission(String submissionText) throws Exception {
        String prompt = createPrompt(submissionText);
        String requestBody = createRequestBody(prompt);
        HttpRequest request = buildHttpRequest(requestBody);
        HttpResponse<String> response = sendRequest(request);
        return parseResponse(response);
    }

    private String createPrompt(String submissionText) {
        return String.format(
            "You are an expert grader for academic assignments. Evaluate the following submission based on clarity, coherence, grammar, relevance, and quality. Provide a brief feedback and assign a score out of 100:\n\n%s",
            submissionText
        );
    }

    private String createRequestBody(String prompt) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", MODEL);
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", MAX_TOKENS);
        requestBody.put("temperature", TEMPERATURE);
        return objectMapper.writeValueAsString(requestBody);
    }

    private HttpRequest buildHttpRequest(String requestBody) {
        return HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
            .build();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private GradingResult parseResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() != 200) {
            throw new RuntimeException("Cohere API error: " + response.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode generations = root.path("generations");
        if (generations.isMissingNode() || generations.size() == 0) {
            throw new RuntimeException("Invalid API response: No generations found.");
        }

        String generatedText = generations.get(0).path("text").asText();
        int grade = extractGrade(generatedText);
        String feedback = extractFeedback(generatedText);
        return new GradingResult(grade, feedback);
    }

    private int extractGrade(String generatedText) {
        String gradePattern = "Score: (\\d{1,3})";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(gradePattern).matcher(generatedText);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new RuntimeException("Grade not found in generated text.");
    }

    private String extractFeedback(String generatedText) {
        int feedbackStart = generatedText.indexOf("Feedback:") + 9;
        if (feedbackStart > 0) {
            return generatedText.substring(feedbackStart).trim();
        }
        throw new RuntimeException("Feedback not found in generated text.");
    }

    public static class GradingResult {
        private final int grade;
        private final String feedback;

        public GradingResult(int grade, String feedback) {
            this.grade = grade;
            this.feedback = feedback;
        }

        public int getGrade() {
            return grade;
        }

        public String getFeedback() {
            return feedback;
        }
    }
}