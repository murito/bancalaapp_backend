package mx.com.nopaltech.bancalawebservice.services;

import mx.com.nopaltech.bancalawebservice.models.Account;
import mx.com.nopaltech.bancalawebservice.models.Transaction;
import mx.com.nopaltech.bancalawebservice.models.Loan;
import mx.com.nopaltech.bancalawebservice.repositories.LoanRepository;
import mx.com.nopaltech.bancalawebservice.repositories.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class PaymentService {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final ServiceRepository serviceRepository; // Asumo que inyectas ServiceRepository aquí
    private final LoanRepository loanRepository;

    @Autowired
    public PaymentService(AccountService accountService, TransactionService transactionService,
                          ServiceRepository serviceRepository, LoanRepository loanRepository) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.serviceRepository = serviceRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional // Asegura que la transacción de débito y el registro sean atómicos
    public Transaction processPayment(String sourceAccountId, String payeeReference, BigDecimal amount, String description) {

        Account sourceAccount = accountService.findById(sourceAccountId)
                .orElseThrow(() -> new RuntimeException("Cuenta de origen no válida."));

        // 1. **VALIDACIÓN**: Verificar que el saldo sea suficiente
        if (sourceAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Saldo insuficiente para realizar el pago.");
        }

        // 2. **DÉBITO**: Actualizar el saldo de la cuenta de origen
        BigDecimal newBalance = sourceAccount.getAvailableBalance().subtract(amount);
        accountService.updateBalance(sourceAccountId, newBalance);

        // 3. **REGISTRO**: Crear y guardar el movimiento de débito
        Transaction debitTransaction = new Transaction();
        debitTransaction.setAccountId(sourceAccountId);
        debitTransaction.setAmount(amount.negate()); // Monto negativo para débito
        debitTransaction.setDescription(description);
        debitTransaction.setType("payment");
        debitTransaction.setReferenceNumber(payeeReference);
        debitTransaction.setStatus("completed");

        return transactionService.save(debitTransaction);
    }

    // Método específico para el abono a un crédito (reutiliza processPayment)
    @Transactional
    public Transaction processLoanPayment(String sourceAccountId, String loanId, BigDecimal amount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado."));

        // 1. Procesa el pago (débito en cuenta de origen y registro en transacciones)
        String description = "Abono a Crédito: " + loan.getLoanName();
        Transaction paymentTransaction = processPayment(sourceAccountId, loanId, amount, description);

        // 2. **ACTUALIZACIÓN**: Lógica para reducir el saldo pendiente del préstamo
        BigDecimal newOutstandingBalance = loan.getOutstandingBalance().subtract(amount);
        loan.setOutstandingBalance(newOutstandingBalance);
        loanRepository.save(loan);

        return paymentTransaction;
    }
}