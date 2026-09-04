package com.sivamachineworks.platform.ai.provider;

import com.sivamachineworks.platform.ai.dto.AiQueryResponse;
import com.sivamachineworks.platform.ai.retrieval.RetrievedBusinessContext;
import com.sivamachineworks.platform.inventory.domain.InventoryStock;
import com.sivamachineworks.platform.production.domain.ProductionOrder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
public class RuleGroundedAiProvider implements AiModelProvider {

    private static final int MAX_QUERY_LENGTH = 1000;
    private static final Set<String> INJECTION_PATTERNS = Set.of(
            "ignore previous instructions", "system override", "jailbreak", "dan mode",
            "dump passwords", "reveal database schema", "developer mode", "bypass security",
            "drop table", "select password_hash", "output secret"
    );

    @Override
    public String getProviderName() {
        return "SivaMachineWorks-SecureRAG-Engine-v1";
    }

    @Override
    public AiQueryResponse generateAnswer(String question, RetrievedBusinessContext context) {
        if (question == null || question.isBlank()) {
            return new AiQueryResponse(
                    question,
                    "Query cannot be empty.",
                    0.0,
                    false,
                    List.of(),
                    0
            );
        }

        // Buffer exhaustion / token flooding guardrail
        if (question.length() > MAX_QUERY_LENGTH) {
            return new AiQueryResponse(
                    question.substring(0, 100) + "...",
                    "Security Policy Violation: Query exceeds maximum permitted length of 1000 characters.",
                    0.0,
                    false,
                    List.of(),
                    0
            );
        }

        String lowerQuery = question.toLowerCase();

        // Guardrail against prompt injection / jailbreak attempts
        for (String pattern : INJECTION_PATTERNS) {
            if (lowerQuery.contains(pattern)) {
                return new AiQueryResponse(
                        question,
                        "Security Policy Violation: I am a read-only manufacturing assistant. Access to system credentials, passwords, or destructive operations is prohibited.",
                        0.0,
                        false,
                        List.of(),
                        0
                );
            }
        }

        // Insufficient Data Check
        if (!context.hasFoundMatchingData() && context.getCitations().isEmpty()) {
            return new AiQueryResponse(
                    question,
                    "I do not have enough verified manufacturing records in the database to answer this query. Please check that the order code, machine model, or supplier name exists.",
                    0.1,
                    false,
                    List.of(),
                    0
            );
        }

        // Grounded synthesis based on verified records
        StringBuilder answerBuilder = new StringBuilder();

        // Case A: Production Order Specific Query
        if (!context.getProductionOrders().isEmpty()) {
            ProductionOrder po = context.getProductionOrders().get(0);
            answerBuilder.append("Production Order ").append(po.getOrderCode())
                    .append(" for machine model ").append(po.getProduct().getProductNumber())
                    .append(" (").append(po.getProduct().getName()).append(") is currently in state '")
                    .append(po.getStatus()).append("'. ");

            if ("PLANNED".equals(po.getStatus())) {
                answerBuilder.append("The order has not started assembly yet because required BOM raw materials must first be reserved in warehouse ")
                        .append(po.getWarehouse() != null ? po.getWarehouse().getCode() : "WH-OSK").append(". ");
            } else if ("IN_PROGRESS".equals(po.getStatus())) {
                answerBuilder.append("Assembly operations are actively underway across work centers. Next milestone is the precision laser metrology Factory Acceptance Test (FAT). ");
            } else if ("COMPLETED".equals(po.getStatus())) {
                answerBuilder.append("Assembly and quality inspection are completed. Finished goods stock has been posted to warehouse inventory. ");
            }
        }

        // Case B: Material Shortages Query
        else if (lowerQuery.contains("shortage") || lowerQuery.contains("material") || lowerQuery.contains("hm-500") || lowerQuery.contains("hm500")) {
            List<InventoryStock> shortages = context.getInventoryStocks().stream()
                    .filter(s -> s.getQtyAvailable().compareTo(new BigDecimal("5.0")) < 0)
                    .toList();

            answerBuilder.append("Analysis of eBOM component availability for SMW-HM-500 Machining Centers: ");
            if (shortages.isEmpty()) {
                answerBuilder.append("All primary components (Hydraulic Pumps, Proportional Valves, CNC Controllers, Servos, Linear Encoders) have sufficient available stock in the Osaka warehouse. ");
            } else {
                answerBuilder.append("Potential bottleneck detected: ");
                for (InventoryStock s : shortages) {
                    answerBuilder.append("Part '").append(s.getPartNumber()).append("' has only ")
                            .append(s.getQtyAvailable()).append(" ").append(s.getUom()).append(" available (")
                            .append(s.getQtyReserved()).append(" currently reserved). ");
                }
                answerBuilder.append("A purchase request (PR) should be issued to certified suppliers to replenish stock. ");
            }
        }

        // Case C: Supplier Delivery Risks
        else if (lowerQuery.contains("supplier") || lowerQuery.contains("delivery risk") || lowerQuery.contains("vendor")) {
            answerBuilder.append("Supplier Delivery & Risk Assessment: ");
            answerBuilder.append("Currently evaluating certified Tier-1 suppliers (Yuken Kogyo for hydraulics, Siemens Japan for CNC motion controllers, Keyence for optical scales). ");
            if (context.getPurchaseOrders().isEmpty()) {
                answerBuilder.append("No delayed purchase orders found in the procurement ledger.");
            } else {
                long pendingCount = context.getPurchaseOrders().stream().filter(po -> !"RECEIVED".equals(po.getStatus())).count();
                answerBuilder.append("There are ").append(pendingCount).append(" open purchase orders in fulfillment. Goods receipt is monitored with atomic ledger logging.");
            }
        }

        // Case D: Customer Orders at Risk
        else if (lowerQuery.contains("customer") || lowerQuery.contains("at risk") || lowerQuery.contains("sales order")) {
            answerBuilder.append("Customer Sales Order Risk Assessment: ");
            long draftCount = context.getSalesOrders().stream().filter(so -> "DRAFT".equals(so.getStatus())).count();
            if (draftCount > 0) {
                answerBuilder.append(draftCount).append(" customer order(s) remain in DRAFT status awaiting confirmation and BOM material reservation.");
            } else {
                answerBuilder.append("All active customer orders are confirmed and progressing through planned production.");
            }
        } else {
            answerBuilder.append("Retrieved business context from manufacturing database: ");
            for (String summary : context.getContextSummaries()) {
                answerBuilder.append(summary).append(" ");
            }
        }

        return new AiQueryResponse(
                question,
                answerBuilder.toString().trim(),
                0.95,
                true,
                context.getCitations(),
                context.getCitations().size()
        );
    }
}
