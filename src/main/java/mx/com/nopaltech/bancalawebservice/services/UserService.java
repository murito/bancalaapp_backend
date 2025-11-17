package mx.com.nopaltech.bancalawebservice.services;

import mx.com.nopaltech.bancalawebservice.models.User;
import mx.com.nopaltech.bancalawebservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    // 3. CONSTRUCTOR ACTUALIZADO para recibir PasswordEncoder
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 4. NUEVO MÉTODO CENTRAL PARA REGISTRO
    public User registerUser(User user) {
        // Asumimos que el campo passwordHash contiene temporalmente la contraseña en texto plano.
        String rawPassword = user.getPasswordHash();

        // Hashear la contraseña con BCrypt antes de guardar
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        // La ejecución de 'save' forzará la creación de la colección 'users'
        return userRepository.save(user);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 5. Método de validación actualizado para usar el Encoder (para el login)
    public boolean validateUser(String email, String rawPassword) {
        User user = findByEmail(email);
        if (user != null) {
            // Compara la contraseña en texto plano con el hash guardado en DB
            return passwordEncoder.matches(rawPassword, user.getPasswordHash());
        }
        return false;
    }
}