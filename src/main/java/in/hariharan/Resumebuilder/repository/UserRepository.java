package in.hariharan.Resumebuilder.repository;

import in.hariharan.Resumebuilder.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    Boolean existsByEmail(String email);
}
