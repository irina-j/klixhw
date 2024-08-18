package org.klix.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.klix.dto.CustomerApplication;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import wiremock.com.google.common.net.HttpHeaders;

import java.math.BigDecimal;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@SpringBootTest
@ActiveProfiles("test")
@ConfigurationPropertiesScan("org.klix.config")
class SolidBankServiceTest {

    @Autowired
    private SolidBankService solidBank;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().port(8081)
                .usingFilesUnderClasspath("wiremock")
                .notifier(new ConsoleNotifier(true)));
        MockitoAnnotations.openMocks(this);
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testSubmitAndPollSolidBank_HappyPath() {
        createPostStub("/applications", "solidBank_Application.json");
        createGetStub("/applications/" + "80134b2a-1492-461c-8fa9-e78d0c28b168", "solidBank_Offer.json");

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(solidBank.submitAndPollSolidBank(customerApplication).log())
                .expectNextMatches(response -> {
                    System.out.println("Received response: " + response);
                    return SourceSystem.SOLID_BANK.equals(response.getSourceSystem())
                            && "PROCESSED".equalsIgnoreCase(response.getStatus());
                })
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    @Test
    void testSubmitAndPollSolidBankSubmissionFailure() {
        wireMockServer.stubFor(post(urlPathEqualTo("/applications"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(solidBank.submitAndPollSolidBank(customerApplication).log())
                .as("Expected function to complete if external system failed")
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    @Test
    void testSubmitAndPollSolidBankSubmissionFailureWithBody() {
        wireMockServer.stubFor(post(urlPathEqualTo("/applications"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withBodyFile("wiremock/__files/testdata/solidBank_Application_FailureWIthBody.json")));

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(solidBank.submitAndPollSolidBank(customerApplication).log())
                .expectNextMatches(response -> {
                    System.out.println("Received response: " + response);
                    return SourceSystem.SOLID_BANK.equals(response.getSourceSystem())
                            && "FAILED".equalsIgnoreCase(response.getStatus());
                })
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    @Test
    void testSubmitAndPollSolidBankOfferRetrievalFailure() {
        createPostStub("/applications", "solidBank_Application.json");

        createGetFailureStub("/applications/" + "80134b2a-1492-461c-8fa9-e78d0c28b168",
                HttpStatus.INTERNAL_SERVER_ERROR);

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(solidBank.submitAndPollSolidBank(customerApplication).log())
                .as("Expected function to complete if call will not be successful in given time")
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    @Test
    void testSubmitAndPollSolidBank_UnexpectedException() {
        createPostStub("/applications", "solidBank_Application.json");

        createGetFailureStub("/applications/" + "80134b2a-1492-461c-8fa9-e78d0c28b168",
                Fault.CONNECTION_RESET_BY_PEER);

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(solidBank.submitAndPollSolidBank(customerApplication).log())
                .as("Expected function to complete if call will not be successful in given time")
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    private void createGetFailureStub(String pathSegment, HttpStatus errorStatus) {
        wireMockServer.stubFor(get(urlPathEqualTo(pathSegment))
                .willReturn(aResponse()
                        .withStatus(errorStatus.value())));
    }

    private void createGetFailureStub(String pathSegment, Fault fault) {
        wireMockServer.stubFor(get(urlPathEqualTo(pathSegment))
                .willReturn(aResponse()
                        .withFault(fault)));
    }

    private void createGetStub(String pathSegment, String responseBodyFileName) {
        wireMockServer.stubFor(get(urlPathEqualTo(pathSegment))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("./testdata/" + responseBodyFileName)));
    }

    private void createPostStub(String pathSegment, String responseBodyFileName) {
        wireMockServer.stubFor(post(urlPathEqualTo(pathSegment))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("./testdata/" + responseBodyFileName)));
    }


    private CustomerApplication getDefaultCustomerApplication() {
        return CustomerApplication.builder()
                .phone("+37145678930")
                .email("john.doe@klix.app")
                .monthlyIncome(BigDecimal.valueOf(3000.75))
                .monthlyExpenses(BigDecimal.valueOf(1500.5))
                .amount(BigDecimal.valueOf(5000.0))
                .maritalStatus("SINGLE")
                .agreeToBeScored(false)
                .dependents(0)
                .build();
    }
}