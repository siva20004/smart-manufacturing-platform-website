package com.sivamachineworks.platform.ai.provider;

import com.sivamachineworks.platform.ai.dto.AiQueryResponse;
import com.sivamachineworks.platform.ai.retrieval.RetrievedBusinessContext;

public interface AiModelProvider {
    AiQueryResponse generateAnswer(String question, RetrievedBusinessContext context);
    String getProviderName();
}
