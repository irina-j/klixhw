package org.klix.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerApplication {

    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String PHONE_NUMBER_PATTERN = "\\+[0-9]{11,15}";

    @NotBlank(message = "Phone cannot be blank")
    @Pattern(regexp = PHONE_NUMBER_PATTERN)
    private String phone;
    @NotBlank(message = "Email cannot be blank")
    @Pattern(regexp = EMAIL_PATTERN)
    private String email;
    @PositiveOrZero(message = "MonthlyIncome cannot be negative")
    @Digits(integer = 7, fraction = 2)
    private BigDecimal monthlyIncome;
    @PositiveOrZero(message = "MonthlyExpenses cannot be negative")
    @Digits(integer = 7, fraction = 2)
    private BigDecimal monthlyExpenses;
    @PositiveOrZero(message = "Amount cannot be negative")
    @Digits(integer = 7, fraction = 2)
    private BigDecimal amount;
    @NotBlank(message = "Marital status is required")
    private String maritalStatus;
    private boolean agreeToBeScored;
    @PositiveOrZero(message = "Dependents cannot be negative")
    private int dependents = 0;

}
