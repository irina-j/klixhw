package org.klix.client.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klix.client.dto.solidbank.ApplicationRequest;
import org.klix.client.dto.solidbank.MaritalStatus;
import org.klix.client.dto.solidbank.SolidOfferResponse;
import org.klix.client.dto.solidbank.SolidRetrieveOfferResponse;
import org.klix.dto.CustomerApplication;
import org.klix.dto.Offer;
import org.klix.dto.Response;
import org.klix.service.SourceSystem;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SolidBankMapperTest {

    private SolidBankMapper solidBankMapper;

    @BeforeEach
    void setUp() {
        solidBankMapper = new SolidBankMapper();
    }

    @Test
    void testMapToRequest() {
        CustomerApplication application = CustomerApplication.builder()
                .phone("123456789")
                .email("test@example.com")
                .monthlyIncome(BigDecimal.valueOf(5000))
                .monthlyExpenses(BigDecimal.valueOf(2000))
                .maritalStatus("SIngLE")
                .agreeToBeScored(true)
                .amount(BigDecimal.valueOf(10000))
                .build();

        ApplicationRequest request = solidBankMapper.mapToRequest(application);

        assertNotNull(request);
        assertEquals("123456789", request.getPhone());
        assertEquals("test@example.com", request.getEmail());
        assertEquals(BigDecimal.valueOf(5000), request.getMonthlyIncome());
        assertEquals(BigDecimal.valueOf(2000), request.getMonthlyExpenses());
        assertEquals(MaritalStatus.SINGLE, request.getMaritalStatus());
        assertTrue(request.isAgreeToBeScored());
        assertEquals(BigDecimal.valueOf(10000), request.getAmount());
    }

    @Test
    void testMapToResponseWithValidSolidRetrieveOfferResponse() {
        SolidOfferResponse offerResponse = SolidOfferResponse.builder()
                .monthlyPaymentAmount(BigDecimal.valueOf(300))
                .totalRepaymentAmount(BigDecimal.valueOf(12000))
                .numberOfPayments(40)
                .annualPercentageRate(BigDecimal.valueOf(5.5))
                .firstRepaymentDate(LocalDate.of(2024, 1, 1).toString())
                .build();

        SolidRetrieveOfferResponse solidRetrieveOfferResponse = SolidRetrieveOfferResponse.builder()
                .offer(offerResponse)
                .status("APPROVED")
                .build();

        Response response = solidBankMapper.mapToResponse(solidRetrieveOfferResponse);

        assertNotNull(response);
        assertEquals(SourceSystem.SOLID_BANK, response.getSourceSystem());
        assertEquals("APPROVED", response.getStatus());

        Offer offer = response.getOffer();
        assertNotNull(offer);
        assertEquals(BigDecimal.valueOf(300), offer.getMonthlyPaymentAmount());
        assertEquals(BigDecimal.valueOf(12000), offer.getTotalRepaymentAmount());
        assertEquals(40, offer.getNumberOfPayments());
        assertEquals(BigDecimal.valueOf(5.5), offer.getAnnualPercentageRate());
        assertEquals(LocalDate.of(2024, 1, 1).toString(), offer.getFirstRepaymentDate());
    }

    @Test
    void testMapToResponseWithNullSolidRetrieveOfferResponse() {
        Response response = solidBankMapper.mapToResponse(null);
        assertNull(response);
    }

    @Test
    void testMapToResponse_withNullOffer() {
        SolidRetrieveOfferResponse solidRetrieveOfferResponse = SolidRetrieveOfferResponse.builder()
                .offer(null)
                .status("REJECTED")
                .build();

        Response response = solidBankMapper.mapToResponse(solidRetrieveOfferResponse);

        assertNotNull(response);
        assertEquals(SourceSystem.SOLID_BANK, response.getSourceSystem());
        assertEquals("REJECTED", response.getStatus());

        Offer offer = response.getOffer();
        assertNotNull(offer);
        assertNull(offer.getMonthlyPaymentAmount());
        assertNull(offer.getTotalRepaymentAmount());
        assertNull(offer.getAnnualPercentageRate());
        assertNull(offer.getFirstRepaymentDate());
    }

}