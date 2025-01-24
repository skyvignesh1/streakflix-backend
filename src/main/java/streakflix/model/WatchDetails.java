package streakflix.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WatchDetails {

    private int trackTime =  0;
    private String movieId;
    private String platformId;
    private int todayWatchedMinutes;
    private String movieName;

}
