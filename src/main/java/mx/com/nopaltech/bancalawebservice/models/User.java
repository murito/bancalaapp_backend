package mx.com.nopaltech.bancalawebservice.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id; // Mapeado al ObjectId de MongoDB

    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phone;

    private UserSettings settings; // Subdocumento anidado

    // Lista de ObjectIds que referencian servicios frecuentes (Colección 'services')
    private List<String> frequentPayees;
}

@Data
class UserSettings {
    private String language;
    private String theme;
}