package org.klix.client.mapper;

import org.klix.client.dto.fastbank.ApplicationRequest;
import org.klix.client.dto.fastbank.FastOfferResponse;
import org.klix.client.dto.fastbank.FastRetrieveOfferResponse;
import org.klix.client.dto.solidbank.SolidOfferResponse;
import org.klix.client.dto.solidbank.SolidRetrieveOfferResponse;
import org.klix.dto.CustomerApplication;
import org.klix.dto.Offer;
import org.klix.dto.Response;
import org.klix.service.SourceSystem;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FastBankRequestMapper {

    public ApplicationRequest mapToRequest(CustomerApplication application) {
        return ApplicationRequest.builder()
                .phoneNumber(application.getPhone())
                .email(application.getEmail())
                .monthlyIncomeAmount(application.getMonthlyIncome())
                .monthlyCreditLiabilities(application.getMonthlyExpenses())
                .dependents(application.getDependents())
                .agreeToDataSharing(application.isAgreeToBeScored())
                .amount(application.getAmount())
                .build();
    }

    public Response mapToResponse(FastRetrieveOfferResponse fastRetrieveOfferResponse) {
        return Optional.ofNullable(fastRetrieveOfferResponse)
                .map(this::buildResponse)
                .orElse(null);
    }

    private Response buildResponse(FastRetrieveOfferResponse fastRetrieveOfferResponse) {
        Offer offer = Optional.ofNullable(fastRetrieveOfferResponse.getOffer())
                .map(this::buildOffer)
                .orElse(new Offer());
        return Response.builder()
                .offer(offer)
                .sourceSystem(SourceSystem.FAST_BANK)
                .status(fastRetrieveOfferResponse.getStatus())
                .build();
    }

    private Offer buildOffer(FastOfferResponse offerResponse) {
        return Offer.builder()
                .monthlyPaymentAmount(offerResponse.getMonthlyPaymentAmount())
                .totalRepaymentAmount(offerResponse.getTotalRepaymentAmount())
                .numberOfPayments(offerResponse.getNumberOfPayments())
                .annualPercentageRate(offerResponse.getAnnualPercentageRate())
                .firstRepaymentDate(offerResponse.getFirstRepaymentDate())
                .build();
    }

}
