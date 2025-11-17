package mx.com.nopaltech.bancalawebservice.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    // Referencia al ID de la cuenta afectada
    private String accountId;

    private Date date;
    private String description;
    private String type; // "debit", "credit", "payment"
    private BigDecimal amount;
    private String status;
    private String category;
    private String referenceNumber;
}
