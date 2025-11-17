package mx.com.nopaltech.bancalawebservice.services;

import mx.com.nopaltech.bancalawebservice.models.Account;
import mx.com.nopaltech.bancalawebservice.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // --- MÉTODOS CRUD BÁSICOS ---

    /**
     * Guarda o actualiza una cuenta. Usado para la creación (alta) y modificación (update).
     */
    public Account save(Account account) {
        // Lógica de negocio durante la creación: si es la primera cuenta, marcarla como primaria.
        if (account.getId() == null) {
            List<Account> existingAccounts = accountRepository.findByUserId(account.getUserId());
            if (existingAccounts.isEmpty()) {
                account.setPrimary(true);
            } else if (account.isPrimary()) {
                // Si el usuario intenta marcarla como primaria en el POST, se llama a setPrimaryAccount después,
                // o se ignora la bandera aquí para que setPrimaryAccount maneje la unicidad.
            }
        }
        return accountRepository.save(account);
    }

    /**
     * Obtiene una cuenta por su ID.
     */
    public Optional<Account> findById(String id) {
        return accountRepository.findById(id);
    }

    /**
     * Elimina una cuenta por su ID.
     */
    public void deleteById(String id) {
        accountRepository.deleteById(id);
    }

    // --- MÉTODOS DE LECTURA ESPECÍFICOS ---

    /**
     * Obtiene todas las cuentas asociadas a un usuario.
     */
    public List<Account> findAccountsByUserId(String userId) {
        return accountRepository.findByUserId(userId);
    }

    // --- LÓGICA DE NEGOCIO PRINCIPAL ---

    /**
     * Marca una cuenta como la cuenta principal del usuario, desmarcando la anterior.
     * @param userId El ID del usuario autenticado.
     * @param newPrimaryAccountId El ID de la cuenta a marcar como primaria.
     * @return La cuenta recién marcada como primaria.
     */
    @Transactional
    public Account setPrimaryAccount(String userId, String newPrimaryAccountId) {

        // 1. Encontrar y desmarcar la cuenta que actualmente es la primaria
        Account currentPrimary = accountRepository.findByUserIdAndIsPrimaryIsTrue(userId);
        if (currentPrimary != null && !currentPrimary.getId().equals(newPrimaryAccountId)) {
            currentPrimary.setPrimary(false);
            accountRepository.save(currentPrimary);
        }

        // 2. Marcar la nueva cuenta como primaria
        Account newPrimary = accountRepository.findById(newPrimaryAccountId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada para marcar como primaria."));

        // Validación de seguridad: Asegurarse de que la cuenta pertenece al usuario
        if (!newPrimary.getUserId().equals(userId)) {
            throw new RuntimeException("La cuenta no pertenece al usuario.");
        }

        newPrimary.setPrimary(true);
        return accountRepository.save(newPrimary);
    }

    /**
     * Actualiza el saldo de una cuenta (usado para transacciones).
     * @param accountId ID de la cuenta.
     * @param amount Monto a sumar/restar (negativo para débito, positivo para crédito).
     * @return La cuenta actualizada.
     */
    @Transactional
    public Account updateBalance(String accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada para actualizar saldo."));

        BigDecimal newBalance = account.getAvailableBalance().add(amount);

        // Validación de saldo negativo (Evitar sobregiro)
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Fondos insuficientes.");
        }

        account.setAvailableBalance(newBalance);
        return accountRepository.save(account);
    }

    /**
     * Obtiene la cuenta principal (isPrimary = true) de un usuario.
     * Utiliza el método del repositorio: findByUserIdAndIsPrimaryIsTrue.
     */
    public Optional<Account> findPrimaryAccountByUserId(String userId) {
        // El método del repositorio devuelve directamente la cuenta o null si no existe.
        Account primaryAccount = accountRepository.findByUserIdAndIsPrimaryIsTrue(userId);

        // Lo envolvemos en Optional para un manejo seguro del nulo.
        return Optional.ofNullable(primaryAccount);
    }

    public Optional<Account> findByUserIdAndAccountId(String userId, String accountId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId);
        return Optional.ofNullable(account);
    }
}