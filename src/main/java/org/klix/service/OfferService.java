package org.klix.service;

import org.klix.dto.CustomerApplication;
import org.klix.dto.Response;
import reactor.core.publisher.Flux;

public interface OfferService {

    Flux<Response> getOffers(CustomerApplication customerApplication);

}
