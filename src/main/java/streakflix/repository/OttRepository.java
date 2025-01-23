package streakflix.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import streakflix.model.Movie;
import streakflix.model.OTTDetails;

@Repository
public interface OttRepository extends MongoRepository<OTTDetails, String> {
}
