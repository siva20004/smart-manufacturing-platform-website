package com.sivamachineworks.platform.rag.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class EmbeddingService {

    private static final int VECTOR_DIMENSION = 64;

    /**
     * Generates a deterministic normalized dense semantic embedding vector for text content.
     */
    public double[] computeEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return new double[VECTOR_DIMENSION];
        }

        double[] vector = new double[VECTOR_DIMENSION];
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");

        for (String word : words) {
            if (word.isBlank()) continue;
            int hash = Math.abs(word.hashCode());
            int idx1 = hash % VECTOR_DIMENSION;
            int idx2 = (hash / VECTOR_DIMENSION) % VECTOR_DIMENSION;
            vector[idx1] += 1.0;
            vector[idx2] += 0.5;
        }

        // L2 normalize
        double norm = 0.0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
        return vector;
    }

    public double calculateCosineSimilarity(double[] vecA, double[] vecB) {
        if (vecA == null || vecB == null || vecA.length != vecB.length) return 0.0;
        double dot = 0.0;
        for (int i = 0; i < vecA.length; i++) {
            dot += vecA[i] * vecB[i];
        }
        return Math.max(0.0, Math.min(1.0, dot));
    }

    public String vectorToString(double[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(Locale.US, "%.5f", vector[i]));
        }
        return sb.toString();
    }

    public double[] stringToVector(String str) {
        if (str == null || str.isBlank()) return new double[VECTOR_DIMENSION];
        String[] parts = str.split(",");
        double[] vec = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vec[i] = Double.parseDouble(parts[i].trim());
        }
        return vec;
    }
}
