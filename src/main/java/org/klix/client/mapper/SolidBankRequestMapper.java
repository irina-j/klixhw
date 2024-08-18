package org.klix.client.mapper;

import org.klix.client.dto.solidbank.ApplicationRequest;
import org.klix.client.dto.solidbank.MaritalStatus;
import org.klix.client.dto.solidbank.SolidOfferResponse;
import org.klix.client.dto.solidbank.SolidRetrieveOfferResponse;
import org.klix.dto.CustomerApplication;
import org.klix.dto.Offer;
import org.klix.dto.Response;
import org.klix.service.SourceSystem;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SolidBankRequestMapper {

    public ApplicationRequest mapToRequest(CustomerApplication application) {
        return ApplicationRequest.builder()
                .phone(application.getPhone())
                .email(application.getEmail())
                .monthlyIncome(application.getMonthlyIncome())
                .monthlyExpenses(application.getMonthlyExpenses())
                .maritalStatus(MaritalStatus.valueOf(application.getMaritalStatus().toUpperCase()))
                .agreeToBeScored(application.isAgreeToBeScored())
                .amount(application.getAmount())
                .build();
    }

    public Response mapToResponse(SolidRetrieveOfferResponse solidRetrieveOfferResponse) {
        return Optional.ofNullable(solidRetrieveOfferResponse)
                .map(this::buildResponse)
                .orElse(null);
    }

    private Response buildResponse(SolidRetrieveOfferResponse solidRetrieveOfferResponse) {
        Offer offer = Optional.ofNullable(solidRetrieveOfferResponse.getOffer())
                .map(this::buildOffer)
                .orElse(new Offer());
        return Response.builder()
                .offer(offer)
                .sourceSystem(SourceSystem.SOLID_BANK)
                .status(solidRetrieveOfferResponse.getStatus())
                .build();
    }

    private Offer buildOffer(SolidOfferResponse offerResponse) {
        return Offer.builder()
                .monthlyPaymentAmount(offerResponse.getMonthlyPaymentAmount())
                .totalRepaymentAmount(offerResponse.getTotalRepaymentAmount())
                .numberOfPayments(offerResponse.getNumberOfPayments())
                .annualPercentageRate(offerResponse.getAnnualPercentageRate())
                .firstRepaymentDate(offerResponse.getFirstRepaymentDate())
                .build();
    }

}
