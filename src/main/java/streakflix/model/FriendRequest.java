package streakflix.model;


import lombok.Data;

@Data
public class FriendRequest {
    private String existingUsername;
    private String friendUsername;
}
