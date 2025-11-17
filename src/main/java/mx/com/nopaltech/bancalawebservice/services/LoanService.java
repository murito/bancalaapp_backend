package mx.com.nopaltech.bancalawebservice.services;

import mx.com.nopaltech.bancalawebservice.models.Loan;
import mx.com.nopaltech.bancalawebservice.repositories.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoanService {
    private final LoanRepository loanRepository;

    @Autowired
    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public List<Loan> getAllLoansByUserId(String userId) {
        return loanRepository.findAllByUserId(userId);
    }

    public Optional<Loan> getLoanById(String loanId){
        return loanRepository.findById(loanId);
    }

    public Loan save(Loan loan) {
        return loanRepository.save(loan);
    }
}
