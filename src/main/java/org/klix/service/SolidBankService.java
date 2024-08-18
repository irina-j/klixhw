package org.klix.service;

import lombok.extern.slf4j.Slf4j;
import org.klix.client.SolidBankApiClient;
import org.klix.client.mapper.SolidBankRequestMapper;
import org.klix.config.PollingConfig;
import org.klix.dto.ApplicationResponse;
import org.klix.dto.CustomerApplication;
import org.klix.dto.Response;
import org.klix.dto.Status;
import org.klix.error.exception.KlixExecutionException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.klix.util.ErrorUtil.buildFailedResponse;
import static org.klix.util.ErrorUtil.onErrorFunction;

@Slf4j
@Service
public class SolidBankService {

    private static final int FIRST_RESPONSE = 1;
    private SourceSystem sourceSystemName;

    private SolidBankApiClient client;
    private SolidBankRequestMapper mapper;
    private PollingConfig solidBankPollingConfig;

    public SolidBankService(SolidBankApiClient client,
                            SolidBankRequestMapper mapper,
                            PollingConfig solidBankPollingConfig) {
        this.sourceSystemName = SourceSystem.SOLID_BANK;
        this.solidBankPollingConfig = solidBankPollingConfig;
        this.client = client;
        this.mapper = mapper;
    }

    public Flux<Response> submitAndPollSolidBank(CustomerApplication customerApplication) {
        return Mono.defer(() -> client.submitApplication(customerApplication))
                .doOnSubscribe(application -> log.info(sourceSystemName + ": Submitting application for email: {}", customerApplication.getEmail()))
                .onErrorResume(e -> onErrorFunction(sourceSystemName, (KlixExecutionException) e))
                .timeout(Duration.ofSeconds(solidBankPollingConfig.getTimeoutSecs()))
                .flatMapMany(response -> {
                    if (Status.FAILED.equalsIgnoreCase(response.getStatus())) {
                        return buildFailedResponse(response);
                    }
                    return pollSolidBankForOffer(response);
                })
                .onErrorResume(e -> {
                    log.error(sourceSystemName + ": Error during submission or polling: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    private Flux<Response> pollSolidBankForOffer(ApplicationResponse applicationResponse) {
        String requestId = applicationResponse.getId();
        return Flux.defer(() -> client.getOfferByApplicationId(requestId))
                .doOnNext(offer -> log.info(sourceSystemName + ": Received offer with status: {} for requestId: {}", offer.getStatus(), requestId))
                .repeatWhen(repeat -> repeat
                        .doOnNext(signal -> log.info(sourceSystemName + ": No Success offer yet, retrying after delay ..."))
                        .delayElements(Duration.ofSeconds(solidBankPollingConfig.getRetryDelaySeconds()))
                        .take(solidBankPollingConfig.getRetryCount()))
                .filter(response -> Status.PROCESSED.equalsIgnoreCase(response.getStatus()))
                .take(FIRST_RESPONSE)
                .timeout(Duration.ofSeconds(solidBankPollingConfig.getTimeoutSecs()))
                .onErrorResume(TimeoutException.class, e -> {
                    log.error(sourceSystemName + ": Polling timed out for requestId: {}", requestId);
                    return Flux.empty();
                })
                .map(mapper::mapToResponse)
                .doOnComplete(() -> log.info(sourceSystemName + ": Polling complete for requestId: {}", requestId));
    }
}
