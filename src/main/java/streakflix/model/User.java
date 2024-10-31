package streakflix.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    @Id
    private String id;
    private String username;
    @Indexed(unique = true)
    private String email;
    private String password;
    private String streak;
    private List<FriendList> friendList;
    private String authorizationToken;
    private String todayWatchedMinutes;

    private String status;

}