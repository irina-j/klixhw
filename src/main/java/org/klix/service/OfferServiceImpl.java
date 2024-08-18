package org.klix.service;

import org.klix.dto.CustomerApplication;
import org.klix.dto.Response;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Objects;

@Service
public class OfferServiceImpl implements OfferService {

    private final FastBankService fastBankService;
    private final SolidBankService solidBankService;

    public OfferServiceImpl(FastBankService fastBankService,
                            SolidBankService solidBankService) {
        this.fastBankService = fastBankService;
        this.solidBankService = solidBankService;
    }

    @Override
    public Flux<Response> getOffers(CustomerApplication customerApplication) {
        return Flux.merge(
                fastBankService.submitAndPollFastBank(customerApplication),
                solidBankService.submitAndPollSolidBank(customerApplication)
        ).filter(Objects::nonNull);
    }

}
