package org.klix.client.dto.solidbank;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ApplicationRequest {

    private String phone;
    private String email;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private MaritalStatus maritalStatus;
    private boolean agreeToBeScored;
    private BigDecimal amount;

}
