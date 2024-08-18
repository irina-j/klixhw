package org.klix.client.dto.fastbank;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ApplicationRequest {

    private String phoneNumber;
    private String email;
    private BigDecimal monthlyIncomeAmount;
    private BigDecimal monthlyCreditLiabilities;
    private int dependents;
    private boolean agreeToDataSharing;
    private BigDecimal amount;

}
