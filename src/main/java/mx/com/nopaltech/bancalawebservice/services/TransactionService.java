package mx.com.nopaltech.bancalawebservice.services;

import mx.com.nopaltech.bancalawebservice.models.Transaction;
import mx.com.nopaltech.bancalawebservice.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction save(Transaction transaction) {
        // Lógica: Asegurar que la fecha esté establecida y que el monto sea válido
        transaction.setDate(new Date());
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findRecentTransactions(String accountId) {
        // Devuelve los movimientos ordenados por fecha descendente (de más reciente a más antiguo)
        return transactionRepository.findByAccountIdOrderByDateDesc(accountId);
    }

    public List<Transaction> findTransactionsInDateRange(String accountId, Date startDate, Date endDate) {
        // Obtiene movimientos para el Estado de Cuenta
        return transactionRepository.findByAccountIdAndDateBetween(accountId, startDate, endDate);
    }
}