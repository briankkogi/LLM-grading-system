package com.sage.sage.util;

import java.util.*;

public class TextIndexer {

    public double calculateCosineSimilarity(String text1, String text2) {
        Map<String, Integer> wordFreq1 = wordFrequencies(text1);
        Map<String, Integer> wordFreq2 = wordFrequencies(text2);

        Set<String> uniqueWords = new HashSet<>(wordFreq1.keySet());
        uniqueWords.addAll(wordFreq2.keySet());

        int[] freqVector1 = new int[uniqueWords.size()];
        int[] freqVector2 = new int[uniqueWords.size()];
        int idx = 0;
        for (String word : uniqueWords) {
            freqVector1[idx] = wordFreq1.getOrDefault(word, 0);
            freqVector2[idx] = wordFreq2.getOrDefault(word, 0);
            idx++;
        }

        return dotProduct(freqVector1, freqVector2) / (magnitude(freqVector1) * magnitude(freqVector2));
    }

    private Map<String, Integer> wordFrequencies(String text) {
        Map<String, Integer> freq = new HashMap<>();
        for (String word : text.split("\\s+")) {
            freq.put(word, freq.getOrDefault(word, 0) + 1);
        }
        return freq;
    }

    private double dotProduct(int[] vector1, int[] vector2) {
        int sum = 0;
        for (int i = 0; i < vector1.length; i++) {
            sum += vector1[i] * vector2[i];
        }
        return sum;
    }

    private double magnitude(int[] vector) {
        int sum = 0;
        for (int value : vector) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }
}