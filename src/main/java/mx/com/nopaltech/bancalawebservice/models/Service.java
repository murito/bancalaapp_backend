package mx.com.nopaltech.bancalawebservice.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "services")
public class Service {

    @Id
    private String id;

    // Referencia al ID del usuario que guardó el servicio
    private String userId;

    private String serviceName;
    private String serviceProvider; // CFE, SIAPA, Telcel
    private String serviceType; // "utility", "airtime", "internet"
    private String referenceNumber;

    private AirtimeDetails airtimeDetails; // Subdocumento (solo si serviceType="airtime")
}
@Data
class AirtimeDetails {
    private String phoneCompany; // Telcel, Movistar, AT&T
}