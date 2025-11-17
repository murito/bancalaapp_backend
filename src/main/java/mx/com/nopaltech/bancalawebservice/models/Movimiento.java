package mx.com.nopaltech.bancalawebservice.models;

import lombok.Data;
import mx.com.nopaltech.bancalawebservice.enums.TipoMovimiento;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Document(collection = "movimientos")
public class Movimiento {
    @Id
    private String id;

    private TipoMovimiento tipo;
    private Date fecha;
    private BigDecimal cantidad;
    private String descripcion;
    private String userId;
    private String accountId;
    private String destinationAccountId;
}
