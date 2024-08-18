package org.klix.service;

import lombok.extern.slf4j.Slf4j;
import org.klix.client.FastBankApiClient;
import org.klix.client.mapper.FastBankRequestMapper;
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
public class FastBankService {

    private static final int FIRST_RESPONSE = 1;

    private SourceSystem sourceSystemName;

    private FastBankApiClient client;
    private FastBankRequestMapper mapper;
    private PollingConfig fastBankPollingConfig;

    public FastBankService(FastBankApiClient client,
                           FastBankRequestMapper mapper,
                           PollingConfig fastBankPollingConfig) {
        this.sourceSystemName = SourceSystem.FAST_BANK;
        this.fastBankPollingConfig = fastBankPollingConfig;
        this.client = client;
        this.mapper = mapper;
    }

    public Flux<Response> submitAndPollFastBank(CustomerApplication customerApplication) {
        return Mono.defer(() -> client.submitApplication(customerApplication))
                .doOnSubscribe(application -> log.info("Submitting application to " + sourceSystemName + " for email: {}", customerApplication.getEmail()))
                .onErrorResume(e -> onErrorFunction(sourceSystemName, (KlixExecutionException) e))
                .timeout(Duration.ofSeconds(fastBankPollingConfig.getTimeoutSecs()))
                .flatMapMany(response -> {
                    if (Status.FAILED.equalsIgnoreCase(response.getStatus())) {
                        return buildFailedResponse(response);
                    }
                    return pollFastBankForOffer(response);
                })
                .onErrorResume(e -> {
                    log.error("Error during submission or polling: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    private Flux<Response> pollFastBankForOffer(ApplicationResponse applicationResponse) {
        String requestId = applicationResponse.getId();
        return Flux.defer(() -> client.getOfferByApplicationId(requestId))
                .doOnNext(offer -> log.info(sourceSystemName + ": Received offer with status: {} for requestId: {}", offer.getStatus(), requestId))
                .repeatWhen(repeat -> repeat
                        .doOnNext(signal -> log.info(sourceSystemName + ": No Success offer yet, retrying after delay ..."))
                        .delayElements(Duration.ofSeconds(fastBankPollingConfig.getRetryDelaySeconds()))
                        .take(fastBankPollingConfig.getRetryCount()))
                .filter(response -> Status.PROCESSED.equalsIgnoreCase(response.getStatus()))
                .take(FIRST_RESPONSE)
                .timeout(Duration.ofSeconds(fastBankPollingConfig.getTimeoutSecs()))
                .onErrorResume(TimeoutException.class, e -> {
                    log.error(sourceSystemName + ": Polling timed out for requestId: {}", requestId);
                    return Flux.empty();
                })
                .map(mapper::mapToResponse)
                .doOnComplete(() -> log.info(sourceSystemName + ": Polling complete for requestId: {}", requestId));
    }

}
