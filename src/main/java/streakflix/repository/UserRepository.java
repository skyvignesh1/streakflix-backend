package streakflix.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import streakflix.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);

    @Query("{ 'username': {'$regex' : '^?0', '$options' : 'i'} }")
    List<User> findMatchingUsers(String username);
}

