package com.sivamachineworks.platform.crm.dto;

import com.sivamachineworks.platform.sales.dto.QuotationResponse;
import com.sivamachineworks.platform.sales.dto.SalesOrderResponse;
import java.util.List;
import java.util.UUID;

public record CustomerHistoryResponse(
    UUID customerId,
    String customerCode,
    String companyName,
    List<ContactResponse> contacts,
    List<OpportunityResponse> opportunities,
    List<QuotationResponse> quotations,
    List<SalesOrderResponse> salesOrders,
    List<ServiceRequestResponse> serviceRequests
) {}
