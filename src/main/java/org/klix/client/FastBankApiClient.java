package org.klix.client;

import lombok.extern.slf4j.Slf4j;
import org.klix.client.dto.fastbank.ApplicationRequest;
import org.klix.client.dto.fastbank.FastRetrieveOfferResponse;
import org.klix.client.dto.fastbank.FastSubmitOfferResponse;
import org.klix.client.mapper.FastBankMapper;
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

@Slf4j
@Component
public class FastBankApiClient {

    @Value("${offers-api.fast-bank.url}")
    private String apiUrl;

    private WebClient webClient;
    private FastBankMapper mapper;

    public FastBankApiClient(WebClient webClient,
                             FastBankMapper mapper) {
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
                .bodyToMono(FastSubmitOfferResponse.class)
                .map(parseSuccessfulResponse())
                .doOnNext(requestId -> log.info("Received requestId from FastBank: {}", requestId));
    }

    public Mono<FastRetrieveOfferResponse> getOfferByApplicationId(String applicationId) {
        return webClient.get()
                .uri(buildURI(apiUrl, "/applications/{id}")
                        .build(applicationId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, ErrorUtil::onErrorFunction)
                .bodyToMono(FastRetrieveOfferResponse.class);
    }

    private static Function<FastSubmitOfferResponse, ApplicationResponse> parseSuccessfulResponse() {
        return response -> ApplicationResponse.builder()
                .id(response.getId())
                .status(response.getStatus().name())
                .build();
    }

    private UriComponentsBuilder buildURI(String apiUrl, String apiPath) {
        return UriComponentsBuilder
                .fromUriString(apiUrl)
                .path(apiPath);
    }

}
