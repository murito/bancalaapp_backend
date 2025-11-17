package mx.com.nopaltech.bancalawebservice.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Data
@Document(collection = "accounts")
public class Account {

    @Id
    private String id;

    // Referencia al ID del usuario propietario
    private String userId;

    private String type; // "debit", "credit"
    private String name;
    private String accountNumber;
    private String lastFourDigits;

    private BigDecimal availableBalance;
    private BigDecimal currentBalance;
    private String currency;
    private boolean isPrimary;

    private CreditDetails creditDetails; // Subdocumento (solo si type="credit")
}

@Data
class CreditDetails {
    private BigDecimal creditLimit;
    private String dueDate;
    private BigDecimal minPayment;
}
