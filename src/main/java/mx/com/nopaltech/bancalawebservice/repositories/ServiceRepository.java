package mx.com.nopaltech.bancalawebservice.repositories;

import mx.com.nopaltech.bancalawebservice.models.Service;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRepository extends MongoRepository<Service, String> {

    // Método para obtener todos los servicios guardados por un usuario
    List<Service> findByUserId(String userId);

    // Método para buscar un servicio específico por su tipo y número de referencia
    Service findByServiceTypeAndReferenceNumber(String serviceType, String referenceNumber);

}