package mx.com.nopaltech.bancalawebservice.controllers;

import mx.com.nopaltech.bancalawebservice.models.Account;
import mx.com.nopaltech.bancalawebservice.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Helper para obtener el ID del usuario autenticado desde el JWT.
     */
    private String getUserId(Authentication authentication) {
        // authentication.getName() devuelve el 'subject' del JWT, que es el userId de MongoDB
        return authentication.getName();
    }

    // --- 1. ALTA (CREATE) ---
    // POST /api/accounts
    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account,
                                                 Authentication authentication) {

        String userId = getUserId(authentication);

        // Asociar la cuenta al usuario autenticado antes de guardar
        account.setUserId(userId);

        // El servicio maneja la lógica de inicialización (ej. saldo 0, isPrimary si es la primera)
        Account newAccount = accountService.save(account);
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }

    // --- 2. LECTURA (READ - Obtener todas las cuentas) ---
    // GET /api/accounts
    @GetMapping
    public ResponseEntity<List<Account>> getAccounts(Authentication authentication) {

        String userId = getUserId(authentication);
        List<Account> accounts = accountService.findAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    // --- 3. LECTURA (READ - Obtener una cuenta por ID) ---
    // GET /api/accounts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable String id,
                                                  Authentication authentication) {

        String userId = getUserId(authentication);

        Optional<Account> accountOptional = accountService.findById(id);

        if (accountOptional.isPresent() && accountOptional.get().getUserId().equals(userId)) {
            return ResponseEntity.ok(accountOptional.get());
        } else {
            // Devolver 404 si no existe O si existe pero no pertenece al usuario
            return ResponseEntity.notFound().build();
        }
    }

    // --- 4. MODIFICACIÓN (UPDATE) ---
    // PUT /api/accounts/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable String id,
                                                 @RequestBody Account accountDetails,
                                                 Authentication authentication) {

        String userId = getUserId(authentication);

        Optional<Account> existingAccountOptional = accountService.findById(id);

        if (existingAccountOptional.isPresent() && existingAccountOptional.get().getUserId().equals(userId)) {
            Account existingAccount = existingAccountOptional.get();

            // Lógica de actualización: solo permitir cambiar ciertos campos
            existingAccount.setName(accountDetails.getName());
            existingAccount.setCurrency(accountDetails.getCurrency());
            // Nota: No se permite cambiar el userId, balance o ID desde aquí.

            Account updatedAccount = accountService.save(existingAccount);
            return ResponseEntity.ok(updatedAccount);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // --- 5. BAJAS (DELETE) ---
    // DELETE /api/accounts/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id,
                                              Authentication authentication) {

        String userId = getUserId(authentication);

        Optional<Account> existingAccountOptional = accountService.findById(id);

        // Verificar si existe y si pertenece al usuario
        if (existingAccountOptional.isPresent() && existingAccountOptional.get().getUserId().equals(userId)) {

            // Lógica: Evitar eliminar cuentas con saldo positivo o cuentas primarias activas.
            if (existingAccountOptional.get().getAvailableBalance().doubleValue() > 0) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request
            }

            accountService.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // --- 6. ENDPOINT DE MARCAR COMO PRIMARIA (YA EXISTENTE) ---
    // PATCH /api/accounts/{accountId}/primary
    @PatchMapping("/{accountId}/primary")
    public ResponseEntity<Account> markAsPrimary(
            @PathVariable String accountId,
            Authentication authentication)
    {
        String userId = getUserId(authentication);

        try {
            Account updatedAccount = accountService.setPrimaryAccount(userId, accountId);
            return ResponseEntity.ok(updatedAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/primary")
    public ResponseEntity<Account> getPrimaryAccount(Authentication authentication) {

        String userId = getUserId(authentication);

        Optional<Account> primaryAccountOptional = accountService.findPrimaryAccountByUserId(userId);

        if (primaryAccountOptional.isPresent()) {
            return ResponseEntity.ok(primaryAccountOptional.get());
        } else {
            // Devuelve 404 si el usuario no tiene ninguna cuenta marcada como primaria
            return ResponseEntity.notFound().build();
        }
    }
}