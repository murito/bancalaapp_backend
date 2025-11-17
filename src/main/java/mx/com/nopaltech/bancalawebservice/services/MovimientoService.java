package mx.com.nopaltech.bancalawebservice.services;

import mx.com.nopaltech.bancalawebservice.models.Movimiento;
import mx.com.nopaltech.bancalawebservice.repositories.MovimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimientoService {
    private final MovimientoRepository movimientoRepository;

    @Autowired
    public MovimientoService(MovimientoRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }

    public List<Movimiento> getAllMovimientosByUserId(String userId) {
        return movimientoRepository.findAllByUserId(userId);
    }

    public Movimiento save(Movimiento movimiento) {
        return movimientoRepository.save(movimiento);
    }
}
