package streakflix.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "streaks")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Streak {
    @Id
    private String id;
    private String username;
    private String streak;
}