package mx.com.nopaltech.bancalawebservice.repositories;

import mx.com.nopaltech.bancalawebservice.models.Loan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanRepository extends MongoRepository<Loan, String> {

    // Método para obtener todos los préstamos o créditos activos de un usuario
    List<Loan> findByUserIdAndStatus(String userId, String status); // Donde status podría ser "active"

    List<Loan> findAllByUserId(String userId);
}