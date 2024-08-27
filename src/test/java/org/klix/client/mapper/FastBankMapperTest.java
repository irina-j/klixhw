package org.klix.client.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klix.client.dto.fastbank.ApplicationRequest;
import org.klix.client.dto.fastbank.FastOfferResponse;
import org.klix.client.dto.fastbank.FastRetrieveOfferResponse;
import org.klix.dto.CustomerApplication;
import org.klix.dto.Response;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FastBankMapperTest {

    private FastBankMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FastBankMapper();
    }

    @Test
    void testMapToRequest() {
        CustomerApplication application = CustomerApplication.builder()
                .phone("+37226000000")
                .email("john.doe@klix.app")
                .monthlyIncome(BigDecimal.valueOf(150.00))
                .monthlyExpenses(BigDecimal.valueOf(10.00))
                .amount(BigDecimal.valueOf(150.00))
                .maritalStatus("MARRIED")
                .agreeToBeScored(true)
                .dependents(0)
                .build();

        ApplicationRequest request = mapper.mapToRequest(application);

        assertNotNull(request);
        assertEquals(application.getPhone(), request.getPhoneNumber());
        assertEquals(application.getEmail(), request.getEmail());
        assertEquals(application.getMonthlyIncome(), request.getMonthlyIncomeAmount());
        assertEquals(application.getMonthlyExpenses(), request.getMonthlyCreditLiabilities());
        assertEquals(application.getDependents(), request.getDependents());
        assertEquals(application.isAgreeToBeScored(), request.isAgreeToDataSharing());
        assertEquals(application.getAmount(), request.getAmount());
    }

    @Test
    void testMapToOffer() {
        FastOfferResponse offerResponse = FastOfferResponse.builder()
                .monthlyPaymentAmount(BigDecimal.valueOf(100.00))
                .totalRepaymentAmount(BigDecimal.valueOf(1200.00))
                .numberOfPayments(12)
                .annualPercentageRate(BigDecimal.valueOf(5.5))
                .firstRepaymentDate("2024-09-01")
                .build();

        FastRetrieveOfferResponse fastRetrieveOfferResponse = FastRetrieveOfferResponse.builder()
                .offer(offerResponse)
                .status("PROCESSED")
                .build();

        Response response = mapper.mapToResponse(fastRetrieveOfferResponse);

        assertNotNull(response);
        assertNotNull(response.getOffer());
        assertEquals(offerResponse.getMonthlyPaymentAmount(), response.getOffer().getMonthlyPaymentAmount());
        assertEquals(offerResponse.getTotalRepaymentAmount(), response.getOffer().getTotalRepaymentAmount());
        assertEquals(offerResponse.getNumberOfPayments(), response.getOffer().getNumberOfPayments());
        assertEquals(offerResponse.getAnnualPercentageRate(), response.getOffer().getAnnualPercentageRate());
        assertEquals(offerResponse.getFirstRepaymentDate(), response.getOffer().getFirstRepaymentDate());
        assertEquals("FAST_BANK", response.getSourceSystem().name());
        assertEquals(fastRetrieveOfferResponse.getStatus(), response.getStatus());
    }

    @Test
    void testMapToOffer_withNullResponse() {
        Response response = mapper.mapToResponse(null);
        assertNull(response);
    }
}