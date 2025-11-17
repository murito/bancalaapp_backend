package mx.com.nopaltech.bancalawebservice.models;

import lombok.Data;
import mx.com.nopaltech.bancalawebservice.enums.TipoPrestamo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Document(collection = "loans")
public class Loan {

    @Id
    private String id;

    // Referencia al ID del usuario propietario
    private String userId;

    private String loanName;
    private BigDecimal originalAmount;
    private BigDecimal outstandingBalance;
    private BigDecimal interestRate;
    private String paymentFrequency;
    private String status;
    private TipoPrestamo tipoPrestamo;
    private Integer numPago;
    private Integer totalPagos;

    private NextPayment nextPayment; // Subdocumento con detalles del próximo pago

    public void setNextPaymentAmount(BigDecimal  amount) {
        this.nextPayment.setAmount(amount);
    }

    public void setNextPaymentDueDate(Date date) {
        this.nextPayment.setDueDate(date);
    }

    public Date getNextPaymentDueDate() {
        return this.nextPayment.getDueDate();
    }

    public BigDecimal getNextPaymentAmount() {
        return this.nextPayment.getAmount();
    }
}

@Data
class NextPayment {
    private BigDecimal amount;
    private Date dueDate;
}