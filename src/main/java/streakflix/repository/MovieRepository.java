package streakflix.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import streakflix.model.Movie;
import streakflix.model.User;

import java.util.List;

public interface MovieRepository extends MongoRepository<Movie, String> {
    @Query("{ 'username': {'$regex' : '^?0', '$options' : 'i'} }")
    List<Movie> findMatchingMovie(String movieName);
    List<Movie> findByPlatform(String platform);
}