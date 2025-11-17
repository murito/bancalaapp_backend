package mx.com.nopaltech.bancalawebservice.repositories;

import mx.com.nopaltech.bancalawebservice.models.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Date;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    // Método para obtener todos los movimientos de una cuenta, ordenados por fecha descendente
    List<Transaction> findByAccountIdOrderByDateDesc(String accountId);

    // Método para obtener movimientos en un rango de fechas (ej: para Estados de Cuenta)
    List<Transaction> findByAccountIdAndDateBetween(String accountId, Date startDate, Date endDate);

}