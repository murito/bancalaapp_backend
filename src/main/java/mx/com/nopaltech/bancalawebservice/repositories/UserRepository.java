package mx.com.nopaltech.bancalawebservice.repositories;

import mx.com.nopaltech.bancalawebservice.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Método personalizado para buscar un usuario por su email (útil para el login)
    User findByEmail(String email);

}