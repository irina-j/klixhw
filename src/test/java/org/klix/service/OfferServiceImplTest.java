package org.klix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klix.dto.CustomerApplication;
import org.klix.dto.Offer;
import org.klix.dto.Response;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

class OfferServiceImplTest {

    @Mock
    private FastBankService fastBankService;

    @Mock
    private SolidBankService solidBankService;

    @InjectMocks
    private OfferServiceImpl offerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetOffers() {
        CustomerApplication customerApplication = CustomerApplication.builder()
                .phone("+37226000000")
                .email("john.doe@klix.app")
                .monthlyIncome(BigDecimal.valueOf(150.00))
                .monthlyExpenses(BigDecimal.valueOf(10.00))
                .amount(BigDecimal.valueOf(150.00))
                .maritalStatus("MARRIED")
                .agreeToBeScored(true)
                .dependents(0)
                .build();

        Response fastBankResponse = new Response(SourceSystem.FAST_BANK, "Processed", new Offer(), null);
        Response solidBankResponse = new Response(SourceSystem.SOLID_BANK, "Processed", new Offer(), null);

        when(fastBankService.submitAndPollFastBank(customerApplication))
                .thenReturn(Flux.just(fastBankResponse));

        when(solidBankService.submitAndPollSolidBank(customerApplication))
                .thenReturn(Flux.just(solidBankResponse));

        Flux<Response> offersFlux = offerService.getOffers(customerApplication);

        StepVerifier.create(offersFlux)
                .expectNext(fastBankResponse)
                .expectNext(solidBankResponse)
                .verifyComplete();
    }

    @Test
    void testGetOffersWithNullResponses() {
        CustomerApplication customerApplication = new CustomerApplication();

        when(fastBankService.submitAndPollFastBank(customerApplication))
                .thenReturn(Flux.empty());

        when(solidBankService.submitAndPollSolidBank(customerApplication))
                .thenReturn(Flux.empty());

        Flux<Response> offersFlux = offerService.getOffers(customerApplication);

        StepVerifier.create(offersFlux)
                .verifyComplete();
    }
}