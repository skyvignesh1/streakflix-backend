package streakflix.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import streakflix.model.Streak;


import java.util.List;
import java.util.Optional;

public interface StreakRepository extends MongoRepository<Streak, String> {
    Optional<Streak> findByUsername(String username);

    @Query("{ 'username': { $in: ?0 } }")
    List<Streak> findByUsernameIn(List<String> usernames);
}