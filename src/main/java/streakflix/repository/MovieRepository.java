package streakflix.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import streakflix.model.Movie;

import java.util.List;

public interface MovieRepository extends MongoRepository<Movie, Movie.CompositeKey> {
    List<Movie> findByCompositeKeyMovieId(String movieId);

    List<Movie> findByCompositeKeyPlatform(String platform);

    @Query(value = "{ 'movieName': {'$regex' : '.*?0.*', '$options' : 'i'} }", sort = "{'streakCount' : -1}")
    List<Movie> findByMovieNameOrderByStreakCountDesc(String  movieName);

    Movie findByCompositeKey(Movie.CompositeKey compositeKey);

}