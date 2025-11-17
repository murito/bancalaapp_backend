package mx.com.nopaltech.bancalawebservice.repositories;

import mx.com.nopaltech.bancalawebservice.models.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    List<Account> findByUserId(String userId);

    Account findByUserIdAndIsPrimaryIsTrue(String userId);

    Account findByIdAndUserId(String accountId, String userId);
}