package com.sivamachineworks.platform.ai.service;

import com.sivamachineworks.platform.ai.dto.AiQueryRequest;
import com.sivamachineworks.platform.ai.dto.AiQueryResponse;
import com.sivamachineworks.platform.ai.provider.AiModelProvider;
import com.sivamachineworks.platform.ai.retrieval.ManufacturingContextRetriever;
import com.sivamachineworks.platform.ai.retrieval.RetrievedBusinessContext;
import com.sivamachineworks.platform.audit.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ManufacturingAiService {

    private final ManufacturingContextRetriever contextRetriever;
    private final AiModelProvider aiModelProvider;
    private final AuditLogService auditLogService;

    public ManufacturingAiService(
            ManufacturingContextRetriever contextRetriever,
            AiModelProvider aiModelProvider,
            AuditLogService auditLogService) {
        this.contextRetriever = contextRetriever;
        this.aiModelProvider = aiModelProvider;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AiQueryResponse processQuery(AiQueryRequest request, UUID userId, String ipAddress) {
        // Step 1: Authorized structured business data retrieval
        RetrievedBusinessContext context = contextRetriever.retrieveContext(request.question());

        // Step 2: Grounded response synthesis via model provider abstraction
        AiQueryResponse response = aiModelProvider.generateAnswer(request.question(), context);

        // Step 3: Security & Compliance Audit Log
        auditLogService.log(
                "AiQuery",
                UUID.randomUUID(),
                "AI_QUERY_EXECUTED",
                "Query: " + request.question() + " | Citations: " + response.citations().size() + " | FoundData: " + response.hasSufficientData(),
                userId,
                ipAddress
        );

        return response;
    }
}
