package mx.com.nopaltech.bancalawebservice.repositories;

import mx.com.nopaltech.bancalawebservice.models.Movimiento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRepository extends MongoRepository<Movimiento, String> {
    List<Movimiento> findAllByUserId(String userId);
}
