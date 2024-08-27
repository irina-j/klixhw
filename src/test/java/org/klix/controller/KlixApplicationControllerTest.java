package org.klix.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klix.dto.CustomerApplication;
import org.klix.dto.Response;
import org.klix.service.OfferService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class KlixApplicationControllerTest {

    @Mock
    private OfferService offerService;

    @InjectMocks
    private KlixApplicationController klixApplicationController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(klixApplicationController).build();
    }

    @Test
    void testGetOffers() {
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
        Response mockResponse = new Response();

        when(offerService.getOffers(any(CustomerApplication.class)))
                .thenReturn(Flux.just(mockResponse));

        webTestClient.post()
                .uri("/v1/offer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(application)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(Response.class)
                .value(responses -> {
                    assertNotNull(responses);
                    assertEquals(1, responses.size());
                    assertEquals(mockResponse, responses.get(0));
                });
    }
}