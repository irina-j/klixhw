package org.klix.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
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
class FastBankServiceTest {

    @Autowired
    private FastBankService fastBankService;

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
    void testSubmitAndPollFastBankRetry() {
        createPostStub("/applications", "fastBank_Application.json");

        wireMockServer.stubFor(get(urlPathEqualTo("/applications/e0190579-a3f1-4911-b44e-8d9c93cf6792"))
                .inScenario("Application Polling")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{ \"status\": \"Draft\" }"))
                .willSetStateTo("Retrying"));

        wireMockServer.stubFor(get(urlPathEqualTo("/applications/e0190579-a3f1-4911-b44e-8d9c93cf6792"))
                .inScenario("Application Polling")
                .whenScenarioStateIs("Retrying")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{ \"status\": \"Draft\" }"))
                .willSetStateTo("Processed"));

        wireMockServer.stubFor(get(urlPathEqualTo("/applications/e0190579-a3f1-4911-b44e-8d9c93cf6792"))
                .inScenario("Application Polling")
                .whenScenarioStateIs("Processed")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("./testdata/fastBank_Offer.json")));

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(fastBankService.submitAndPollFastBank(customerApplication).log())
                .expectNextMatches(response -> {
                    System.out.println("Received response: " + response);
                    return SourceSystem.FAST_BANK.equals(response.getSourceSystem())
                            && "PROCESSED".equalsIgnoreCase(response.getStatus());
                })
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    @Test
    void testSubmitAndPollFastBankHappyPath() {
        createPostStub("/applications", "fastBank_Application.json");
        createGetStub("/applications/" + "e0190579-a3f1-4911-b44e-8d9c93cf6792", "fastBank_Offer.json");

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(fastBankService.submitAndPollFastBank(customerApplication).log())
                .expectNextMatches(response -> {
                    System.out.println("Received response: " + response);
                    return SourceSystem.FAST_BANK.equals(response.getSourceSystem())
                            && "PROCESSED".equalsIgnoreCase(response.getStatus());
                })
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    @Test
    void testSubmitAndPollFastBankSubmissionFailure() {
        wireMockServer.stubFor(post(urlPathEqualTo("/applications"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(fastBankService.submitAndPollFastBank(customerApplication).log())
                .as("Expected function to complete if external system failed")
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    @Test
    void testSubmitAndPollFastBankSubmissionFailureWithBody() {
        wireMockServer.stubFor(post(urlPathEqualTo("/applications"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withBodyFile("./testdata/fastBank_Application_FailureWIthBody.json")));

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(fastBankService.submitAndPollFastBank(customerApplication).log())
                .expectNextMatches(response -> {
                    System.out.println("Received response: " + response);
                    return SourceSystem.FAST_BANK.equals(response.getSourceSystem())
                            && "FAILED".equalsIgnoreCase(response.getStatus());
                })
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    @Test
    void testSubmitAndPollFastBankOfferRetrievalFailure() {
        createPostStub("/applications", "fastBank_Application.json");

        createGetFailureStub("/applications/" + "e0190579-a3f1-4911-b44e-8d9c93cf6792",
                HttpStatus.INTERNAL_SERVER_ERROR);

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(fastBankService.submitAndPollFastBank(customerApplication).log())
                .as("Expected function to complete if call will not be successful in given time")
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    @Test
    void testSubmitAndPollFastBank_UnexpectedException() {
        createPostStub("/applications", "fastBank_Application.json");

        createGetFailureStub("/applications/" + "e0190579-a3f1-4911-b44e-8d9c93cf6792",
                Fault.CONNECTION_RESET_BY_PEER);

        CustomerApplication customerApplication = getDefaultCustomerApplication();

        StepVerifier.create(fastBankService.submitAndPollFastBank(customerApplication).log())
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
                .phone("+37226000000")
                .email("john.doe@klix.app")
                .monthlyIncome(BigDecimal.valueOf(150.00))
                .monthlyExpenses(BigDecimal.valueOf(10.00))
                .amount(BigDecimal.valueOf(150.00))
                .maritalStatus("MARRIED")
                .agreeToBeScored(true)
                .dependents(0)
                .build();
    }
}