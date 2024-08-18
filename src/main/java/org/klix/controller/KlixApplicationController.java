package org.klix.controller;

import jakarta.validation.Valid;
import org.klix.dto.CustomerApplication;
import org.klix.dto.Response;
import org.klix.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class KlixApplicationController {

    private final OfferService offerService;

    @Autowired
    public KlixApplicationController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping(value = "/offer")
    public Flux<ServerSentEvent<Response>> getOffers(@Valid @RequestBody CustomerApplication request) {
        return offerService.getOffers(request).map(offer -> ServerSentEvent.builder(offer).build());
    }
}
