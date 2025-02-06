package streakflix.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@Document(collection = "movies")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Movie {

    @Id
    @Indexed(unique=true)
    private CompositeKey compositeKey;

    private String movieName;
    private String originalTitle;
    private int actualDuration;
    private int streakCount;

    private String moviePosterURL;
    private String movieDescription;
    private String backDropURL;

    private List<String> genre;

    @Data
    @AllArgsConstructor
    public static class CompositeKey implements Serializable {
        private String movieId;
        private String platform;
    }

    @Override
    public boolean equals(Object obj){

        if (getClass() != obj.getClass())
            return false;

        Movie movie = (Movie) obj;
        return this.getCompositeKey().getMovieId().equals(movie.getCompositeKey().getMovieId()) &&
                this.getCompositeKey().getPlatform().equals(movie.getCompositeKey().getPlatform());
    }
}