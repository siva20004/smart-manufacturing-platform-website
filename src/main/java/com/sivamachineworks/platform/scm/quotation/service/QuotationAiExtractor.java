package com.sivamachineworks.platform.scm.quotation.service;

import com.sivamachineworks.platform.scm.domain.Supplier;
import com.sivamachineworks.platform.scm.repository.SupplierRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class QuotationAiExtractor {

    private final SupplierRepository supplierRepository;

    public QuotationAiExtractor(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public ExtractionResult extract(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ExtractionResult(null, null, null, null, null, null, "JPY", null, "Net 30 Days", BigDecimal.ZERO, List.of("Document text is empty or unreadable"));
        }

        List<String> warnings = new ArrayList<>();
        double confidencePoints = 0.0;
        double totalPoints = 6.0;

        // 1. Supplier Matching
        Supplier matchedSupplier = null;
        String extractedSupplierName = null;
        List<Supplier> suppliers = supplierRepository.findAll();
        for (Supplier s : suppliers) {
            if (rawText.toLowerCase().contains(s.getCompanyName().toLowerCase()) || rawText.toLowerCase().contains(s.getSupplierCode().toLowerCase())) {
                matchedSupplier = s;
                extractedSupplierName = s.getCompanyName();
                confidencePoints += 1.0;
                break;
            }
        }

        String quotationNumber = null;
        String partNumber = null;
        BigDecimal quantity = null;
        BigDecimal unitPrice = null;
        String currency = "JPY";
        if (rawText.contains("$") || rawText.toUpperCase().contains("USD")) currency = "USD";
        if (rawText.contains("€") || rawText.toUpperCase().contains("EUR")) currency = "EUR";
        LocalDate deliveryDate = null;
        String paymentTerms = "Net 30 Days";

        // Line-by-line parsing
        String[] lines = rawText.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            String lower = line.toLowerCase();

            // Supplier Name from header line if not matched
            if (matchedSupplier == null && extractedSupplierName == null && (lower.startsWith("supplier:") || lower.startsWith("vendor:") || lower.startsWith("from:"))) {
                extractedSupplierName = line.substring(line.indexOf(":") + 1).trim();
                confidencePoints += 0.5;
            }

            // Quotation Number
            if (quotationNumber == null && (lower.startsWith("quotation no") || lower.startsWith("quote no") || lower.startsWith("quotation #") || lower.startsWith("quote #") || lower.startsWith("ref no") || lower.startsWith("quote ref") || lower.startsWith("quotation:") || lower.startsWith("quote:"))) {
                int colonIdx = line.indexOf(":");
                if (colonIdx != -1) {
                    quotationNumber = line.substring(colonIdx + 1).trim().split("\\s+")[0];
                } else {
                    String[] parts = line.split("\\s+");
                    if (parts.length > 2) quotationNumber = parts[2].trim();
                }
                if (quotationNumber != null && !quotationNumber.isBlank()) {
                    confidencePoints += 1.0;
                }
            }

            // Part Number
            if (partNumber == null && (lower.startsWith("part no") || lower.startsWith("part #") || lower.startsWith("item no") || lower.startsWith("part:") || lower.startsWith("item:") || lower.startsWith("model:") || lower.startsWith("sku:"))) {
                int colonIdx = line.indexOf(":");
                if (colonIdx != -1) {
                    partNumber = line.substring(colonIdx + 1).trim().split("\\s+")[0];
                } else {
                    String[] parts = line.split("\\s+");
                    if (parts.length > 2) partNumber = parts[2].trim();
                }
                if (partNumber != null && !partNumber.isBlank()) {
                    confidencePoints += 1.0;
                }
            }

            // Quantity
            if (quantity == null && (lower.startsWith("quantity") || lower.startsWith("qty"))) {
                Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    try {
                        quantity = new BigDecimal(m.group(1));
                        if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                            confidencePoints += 1.0;
                        }
                    } catch (Exception ignored) {}
                }
            }

            // Unit Price
            if (unitPrice == null && (lower.startsWith("unit price") || lower.startsWith("unit rate") || lower.startsWith("price:"))) {
                Pattern p = Pattern.compile("([\\d,]+(?:\\.\\d+)?)");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    try {
                        unitPrice = new BigDecimal(m.group(1).replace(",", ""));
                        if (unitPrice.compareTo(BigDecimal.ZERO) > 0) {
                            confidencePoints += 1.0;
                        }
                    } catch (Exception ignored) {}
                }
            }

            // Delivery Date
            if (deliveryDate == null && (lower.startsWith("delivery") || lower.startsWith("valid until") || lower.startsWith("ship date") || lower.startsWith("lead time"))) {
                Pattern p = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    try {
                        deliveryDate = LocalDate.parse(m.group(1));
                        confidencePoints += 1.0;
                    } catch (Exception ignored) {}
                }
            }

            // Payment Terms
            if (lower.startsWith("payment terms") || lower.startsWith("terms")) {
                if (line.contains(":")) {
                    paymentTerms = line.substring(line.indexOf(":") + 1).trim();
                }
            }
        }

        // Direct regex fallbacks
        if (partNumber == null) {
            Pattern directPart = Pattern.compile("(?i)(PUMP-[A-Z0-9-]+|VLV-[A-Z0-9-]+|PLC-[A-Z0-9-]+|MTR-[A-Z0-9-]+|SEN-[A-Z0-9-]+)");
            Matcher directMatcher = directPart.matcher(rawText);
            if (directMatcher.find()) {
                partNumber = directMatcher.group(1).trim().toUpperCase();
                confidencePoints += 1.0;
            }
        }

        if (quotationNumber == null) {
            warnings.add("Quotation number not found");
        }
        if (partNumber == null) {
            warnings.add("Part number could not be determined");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            warnings.add("Quantity could not be extracted or is invalid");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            warnings.add("Unit price could not be extracted or is invalid");
        }
        if (deliveryDate == null) {
            deliveryDate = LocalDate.now().plusDays(30);
            warnings.add("Delivery date not explicitly specified; defaulted to +30 days");
        }

        BigDecimal confidenceScore = BigDecimal.valueOf(confidencePoints / totalPoints).setScale(4, RoundingMode.HALF_UP);

        return new ExtractionResult(
                matchedSupplier,
                extractedSupplierName,
                quotationNumber,
                partNumber,
                quantity,
                unitPrice,
                currency,
                deliveryDate,
                paymentTerms,
                confidenceScore,
                warnings
        );
    }

    public record ExtractionResult(
            Supplier matchedSupplier,
            String supplierNameExtracted,
            String quotationNumber,
            String partNumber,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String currency,
            LocalDate deliveryDate,
            String paymentTerms,
            BigDecimal confidenceScore,
            List<String> warnings
    ) {}
}
