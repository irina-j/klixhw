package org.klix.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.klix.dto.ApplicationResponse;
import org.klix.dto.Response;
import org.klix.error.PredefinedErrors;
import org.klix.error.exception.KlixExecutionException;
import org.klix.service.SourceSystem;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.klix.dto.Status.FAILED;

@Slf4j
public class ErrorUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Mono<KlixExecutionException> onErrorFunction(ClientResponse clientResponse) {
        log.error("Received error status: {}", clientResponse.statusCode());
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    KlixExecutionException errorResponse;
                    try {
                        errorResponse = parseErrorResponse(errorBody);
                    } catch (Exception e) {
                        log.error("Error parsing error body: {}", e.getMessage());
                        errorResponse = createFallbackErrorResponse(e);
                    }
                    return Mono.just(errorResponse);
                });
    }

    public static Mono<ApplicationResponse> onErrorFunction(SourceSystem sourceSystem, KlixExecutionException e) {
        log.error("Error submitting application to {}; errorId: {}", sourceSystem, e.getErrorId());
        ApplicationResponse errorResponse = new ApplicationResponse();
        errorResponse.setSourceSystem(sourceSystem);
        errorResponse.setStatus(FAILED);
        errorResponse.setErrors(e.getErrors());
        return Mono.just(errorResponse);
    }

    public static Flux<Response> buildFailedResponse(ApplicationResponse response) {
        log.error("Submission failed: {}", response.getErrors());
        return Flux.just(Response.builder()
                .sourceSystem(response.getSourceSystem())
                .status(response.getStatus())
                .errors(response.getErrors())
                .build());
    }

    private static KlixExecutionException createFallbackErrorResponse(Exception e) {
        return new KlixExecutionException(
                UUID.randomUUID().toString(),
                PredefinedErrors.INTERNAL_SERVER_ERROR,
                e.getMessage(),
                null,
                e.getCause(),
                Collections.emptyList(),
                LocalDateTime.now().toString()
        );
    }

    private static KlixExecutionException parseErrorResponse(String errorBody) {
        try {
            return mapper.readValue(errorBody, KlixExecutionException.class);
        } catch (IOException e) {
            throw new KlixExecutionException("Error parsing error response", e.getMessage(), e.getCause());
        }
    }

}
