package org.klix.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Offer {

    private String sourceBank;
    private BigDecimal monthlyPaymentAmount;
    private BigDecimal totalRepaymentAmount;
    private int numberOfPayments;
    private BigDecimal annualPercentageRate;
    private String firstRepaymentDate;

}
