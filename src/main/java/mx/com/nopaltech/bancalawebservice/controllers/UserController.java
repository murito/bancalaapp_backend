package mx.com.nopaltech.bancalawebservice.controllers;

import mx.com.nopaltech.bancalawebservice.dto.LoanPaymentDTO;
import mx.com.nopaltech.bancalawebservice.enums.TipoMovimiento;
import mx.com.nopaltech.bancalawebservice.models.*;
import mx.com.nopaltech.bancalawebservice.services.AccountService;
import mx.com.nopaltech.bancalawebservice.services.LoanService;
import mx.com.nopaltech.bancalawebservice.services.MovimientoService;
import mx.com.nopaltech.bancalawebservice.services.UserService;
import mx.com.nopaltech.bancalawebservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    private final MovimientoService movimientoService;
    private final AccountService accountService;
    private final LoanService loanService;

    @Autowired
    public UserController(
            UserService userService,
            MovimientoService movimientoService,
            AccountService accountService,
            LoanService loanService,
            JwtUtil jwtUtil
    ) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.movimientoService = movimientoService;
        this.accountService = accountService;
        this.loanService = loanService;
    }

    // Clase POJO para la petición de login
    static class LoginRequest {
        public String email;
        public String password;

        // Deben incluirse getters y setters reales
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // -----------------------------------------------------------------
    // NUEVO ENDPOINT TEMPORAL DE REGISTRO
    // POST /api/users/register
    // -----------------------------------------------------------------
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> registerUser(@RequestBody User user) {

        // El servicio se encarga de hashear y guardar.
        User createdUser = userService.registerUser(user);

        // Limpiar la contraseña antes de devolver el objeto
        createdUser.setPasswordHash(null);

        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // Endpoint para el Login y generación de JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        // 1. Validar las credenciales usando el servicio
        // NOTA: La validación debe incluir la comparación del hash de la contraseña.
        boolean isValid = userService.validateUser(
                loginRequest.email,
                loginRequest.password
        );

        if (isValid) {
            User user = userService.findByEmail(loginRequest.email);

            // 2. Generar el JWT
            final String jwt = jwtUtil.generateToken(user.getId());

            // 3. Devolver el token y la información básica
            Map<String, Object> response = Map.of(
                    "token", jwt,
                    "userId", user.getId(),
                    "firstName", user.getFirstName()
            );

            return new ResponseEntity<>(response, HttpStatus.OK);

        } else {
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }
    }

    // Endpoint para obtener información (Ahora protegido por JWT)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserInfo(@PathVariable String id) {

        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPasswordHash(null); // Limpiar datos sensibles
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<?> getUserMovimientos(@PathVariable String id) {
        List<Movimiento> movimientos =  movimientoService.getAllMovimientosByUserId(id);
        return new ResponseEntity<>(movimientos, HttpStatus.OK);
    }

    @GetMapping("/{id}/movimientos/{accountId}")
    public ResponseEntity<?> getUserMovimientosByAccount(@PathVariable String id,  @PathVariable String accountId) {
        List<Movimiento> movimientos =  movimientoService.getAllMovimientosByUserId(id);

        List<Movimiento> filteredMovimientos = movimientos.stream()
                .filter(m -> {
                    if ( m.getAccountId() != null ) {
                        return m.getAccountId().equals(accountId);
                    }
                    return false;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(filteredMovimientos, HttpStatus.OK);
    }

    @PostMapping("/{id}/movimiento")
    public ResponseEntity<?> addMovimiento(@RequestBody Movimiento movimiento, @PathVariable String id) {
        Optional<Account> account = accountService.findByUserIdAndAccountId(id, movimiento.getAccountId());

        List<Movimiento> createdMovimientos = new ArrayList<>();

        if( TipoMovimiento.TRANSFERENCIA.equals(movimiento.getTipo()) ){
            Optional<Account> destinationAccount = accountService.findById(movimiento.getDestinationAccountId());

            if( account.isPresent() && destinationAccount.isPresent() ) {
                Account source =  account.get();
                Account destination = destinationAccount.get();

                // Subtract amount from source
                BigDecimal balance = source.getCurrentBalance();
                balance = balance.subtract(movimiento.getCantidad());
                source.setCurrentBalance(balance);
                accountService.save(source);

                // Create the gasto for the source
                Movimiento gasto = new Movimiento();
                gasto.setTipo(TipoMovimiento.GASTO);
                gasto.setFecha(movimiento.getFecha());
                gasto.setCantidad(movimiento.getCantidad());
                gasto.setDescripcion(movimiento.getDescripcion());
                gasto.setUserId(movimiento.getUserId());
                gasto.setAccountId(movimiento.getAccountId());
                movimientoService.save(gasto);

                // Add amount to destination
                BigDecimal destinationBalance = destination.getCurrentBalance();
                destinationBalance = destination.getType().equals("debit") ?
                        destinationBalance.add(movimiento.getCantidad()):
                        destinationBalance.subtract(movimiento.getCantidad());
                destination.setCurrentBalance(destinationBalance);
                accountService.save(destination);

                // Main movement
                Movimiento ingreso = new Movimiento();
                ingreso.setTipo(TipoMovimiento.INGRESO);
                ingreso.setFecha(movimiento.getFecha());
                ingreso.setCantidad(movimiento.getCantidad());
                ingreso.setDescripcion(movimiento.getDescripcion());
                ingreso.setUserId(movimiento.getUserId());
                ingreso.setAccountId(movimiento.getDestinationAccountId());
                movimientoService.save(ingreso);

                createdMovimientos.add(gasto);
                createdMovimientos.add(ingreso);
            }
        }else {
            if (account.isPresent() && TipoMovimiento.GASTO.equals(movimiento.getTipo())) {
                Account acc =  account.get();
                BigDecimal balance = acc.getCurrentBalance();
                balance = balance.subtract(movimiento.getCantidad());
                acc.setCurrentBalance(balance);
                accountService.save(acc);

                createdMovimientos.add(movimientoService.save(movimiento));
            }

            if (account.isPresent() && TipoMovimiento.INGRESO.equals(movimiento.getTipo())) {
                Account acc =  account.get();
                BigDecimal balance = acc.getCurrentBalance();
                balance = balance.add(movimiento.getCantidad());
                acc.setCurrentBalance(balance);
                accountService.save(acc);

                createdMovimientos.add(movimientoService.save(movimiento));
            }
        }

        if( !createdMovimientos.isEmpty() ) {
            return new ResponseEntity<>(createdMovimientos, HttpStatus.CREATED);
        }else  {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("{id}/loan")
    public ResponseEntity<?> addLoan(@RequestBody Loan loan, @PathVariable String id) {
        loan.setUserId(id);
        Loan createdLoan = loanService.save(loan);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }

    @GetMapping("{id}/loans")
    public ResponseEntity<?> getLoans(@PathVariable String id) {
        List<Loan> loans = loanService.getAllLoansByUserId(id);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    @PostMapping("{id}/loan/{loanId}/pago")
    public ResponseEntity<?> payLoan(@RequestBody LoanPaymentDTO payment, @PathVariable String id, @PathVariable String loanId) {
        Optional<Loan> loan = loanService.getLoanById(loanId);
        Optional<Account> account = accountService.findById(payment.getAccountId());

        Movimiento movimiento = null;

        if( loan.isPresent() && account.isPresent() ) {
            Loan currentLoan = loan.get();
            currentLoan.setOutstandingBalance(currentLoan.getOutstandingBalance().subtract(payment.getAmount()));
            currentLoan.setNumPago(currentLoan.getNumPago() + 1);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentLoan.getNextPaymentDueDate());
            calendar.add(Calendar.MONTH, 1);

            // Add one month for the next payment
            currentLoan.setNextPaymentDueDate( calendar.getTime() );

            // save the loan
            loanService.save(currentLoan);

            // subtract funds from the account
            Account acc = account.get();
            acc.setCurrentBalance(acc.getCurrentBalance().subtract(payment.getAmount()));
            accountService.save(acc);

            // Add the movement on the user account
            movimiento = new Movimiento();
            movimiento.setTipo(TipoMovimiento.GASTO);
            movimiento.setFecha(new Date());
            movimiento.setCantidad(payment.getAmount());
            movimiento.setDescripcion("Abono a credito: " + currentLoan.getLoanName() );
            movimiento.setUserId(id);
            movimiento.setAccountId(payment.getAccountId());
            movimientoService.save(movimiento);

            return new ResponseEntity<>(movimiento, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}