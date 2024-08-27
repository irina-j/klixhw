package org.klix.client;

import lombok.extern.log4j.Log4j2;
import org.klix.client.dto.solidbank.ApplicationRequest;
import org.klix.client.dto.solidbank.SolidRetrieveOfferResponse;
import org.klix.client.dto.solidbank.SolidSubmitOfferResponse;
import org.klix.client.mapper.SolidBankMapper;
import org.klix.dto.ApplicationResponse;
import org.klix.dto.CustomerApplication;
import org.klix.util.ErrorUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Log4j2
@Component
public class SolidBankApiClient {

    @Value("${offers-api.solid-bank.url}")
    private String apiUrl;

    private WebClient webClient;
    private SolidBankMapper mapper;

    public SolidBankApiClient(WebClient webClient,
                              SolidBankMapper mapper) {
        this.webClient = webClient;
        this.mapper = mapper;
    }

    public Mono<ApplicationResponse> submitApplication(CustomerApplication application) {
        ApplicationRequest request = mapper.mapToRequest(application);
        return webClient.post()
                .uri(apiUrl + "/applications")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ErrorUtil::onErrorFunction)
                .bodyToMono(SolidSubmitOfferResponse.class)
                .map(parseSuccessfulResponse())
                .doOnNext(requestId -> log.info("Received requestId from SolidBank: {}", requestId));
    }

    private static Function<SolidSubmitOfferResponse, ApplicationResponse> parseSuccessfulResponse() {
        return response -> ApplicationResponse.builder()
                .id(response.getId())
                .status(response.getStatus().name())
                .build();
    }

    public Mono<SolidRetrieveOfferResponse> getOfferByApplicationId(String applicationId) {
        return webClient.get()
                .uri(buildURI(apiUrl, "/applications/{id}")
                        .build(applicationId))
                .retrieve()
                .bodyToMono(SolidRetrieveOfferResponse.class);
    }

    private UriComponentsBuilder buildURI(String apiUrl, String apiPath) {
        return UriComponentsBuilder
                .fromUriString(apiUrl)
                .path(apiPath);
    }

}
