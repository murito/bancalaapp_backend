package mx.com.nopaltech.bancalawebservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanPaymentDTO {
    private BigDecimal amount;
    private String accountId;
}
