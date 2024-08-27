package org.klix.client.dto.solidbank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolidOfferResponse {

    private BigDecimal monthlyPaymentAmount;
    private BigDecimal totalRepaymentAmount;
    private int numberOfPayments;
    private BigDecimal annualPercentageRate;
    private String firstRepaymentDate;

}
