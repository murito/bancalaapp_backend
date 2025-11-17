package mx.com.nopaltech.bancalawebservice.security;

import mx.com.nopaltech.bancalawebservice.models.User;
import mx.com.nopaltech.bancalawebservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Este método es llamado por Spring Security para cargar los datos del usuario.
     * Aunque el método se llama loadUserByUsername, aquí lo usamos para cargar por el ID
     * de MongoDB que extraemos del JWT.
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        Optional<User> userOptional = userService.findById(userId);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Usuario con ID: " + userId + " no encontrado.");
        }

        User user = userOptional.get();

        // Mapea tu objeto User de MongoDB a la interfaz UserDetails de Spring Security.
        // El primer parámetro (username) es el ID del usuario.
        return new org.springframework.security.core.userdetails.User(
                user.getId(), // Usamos el ID de MongoDB como nombre de usuario (principal)
                user.getPasswordHash(), // Contraseña hasheada
                new ArrayList<>() // Lista de roles/autoridades (vacía o con roles definidos)
        );
    }
}