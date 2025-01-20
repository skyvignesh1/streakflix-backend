package streakflix.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "movies")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Movie {
    @Id
    private String MovieId;
    private String platform;
    private String movieName;
    private int actualDuration;
    private int streakCount;
}