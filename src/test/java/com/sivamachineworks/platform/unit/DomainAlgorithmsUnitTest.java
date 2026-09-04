package com.sivamachineworks.platform.unit;

import com.sivamachineworks.platform.rag.service.EmbeddingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainAlgorithmsUnitTest {

    private final EmbeddingService embeddingService = new EmbeddingService();

    @Test
    public void test1_embeddingService_generatesNormalizedVectors() {
        String text1 = "Precision CNC Horizontal Machining Center Spindle Maintenance";
        String text2 = "Hydraulic variable displacement axial piston pump assembly";
        String text3 = "CNC Horizontal Machining Center Spindle calibration";

        double[] emb1 = embeddingService.computeEmbedding(text1);
        double[] emb2 = embeddingService.computeEmbedding(text2);
        double[] emb3 = embeddingService.computeEmbedding(text3);

        assertThat(emb1).isNotNull().hasSize(64);
        assertThat(emb2).isNotNull().hasSize(64);

        double norm1 = 0.0;
        for (double v : emb1) norm1 += v * v;
        assertThat(Math.sqrt(norm1)).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.01));

        double simRelated = embeddingService.calculateCosineSimilarity(emb1, emb3);
        double simUnrelated = embeddingService.calculateCosineSimilarity(emb1, emb2);

        assertThat(simRelated).isGreaterThan(simUnrelated);
    }

    @Test
    public void test2_quotationParsingUnit_extractsDeterministicData() {
        String raw = """
                OFFICIAL SUPPLIER QUOTATION
                Supplier: Yuken Kogyo Co., Ltd.
                Quotation No: QUO-TEST-1001
                Part No: PUMP-HP-75
                Quantity: 25
                Unit Price: ¥185,000
                Delivery Date: 2026-11-20
                Payment Terms: Net 45 Days
                """;

        String[] lines = raw.split("\\r?\\n");
        String quoteNo = null;
        String partNo = null;
        BigDecimal qty = null;
        BigDecimal price = null;

        for (String line : lines) {
            String lower = line.toLowerCase();
            if (lower.startsWith("quotation no:")) {
                quoteNo = line.substring(line.indexOf(":") + 1).trim();
            }
            if (lower.startsWith("part no:")) {
                partNo = line.substring(line.indexOf(":") + 1).trim();
            }
            if (lower.startsWith("quantity:")) {
                qty = new BigDecimal(line.substring(line.indexOf(":") + 1).trim());
            }
            if (lower.startsWith("unit price:")) {
                String clean = line.replaceAll("[^0-9.]", "");
                price = new BigDecimal(clean);
            }
        }

        assertThat(quoteNo).isEqualTo("QUO-TEST-1001");
        assertThat(partNo).isEqualTo("PUMP-HP-75");
        assertThat(qty).isEqualByComparingTo(new BigDecimal("25"));
        assertThat(price).isEqualByComparingTo(new BigDecimal("185000"));
    }
}
