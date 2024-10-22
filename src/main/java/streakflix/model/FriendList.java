package streakflix.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FriendList {
    @Indexed(unique = true)
    private String username;
    private String status;
    private String streak;
}