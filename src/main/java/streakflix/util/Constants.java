package streakflix.util;

import java.util.*;

public class Constants {

    public static final Map<String, String> GENRE_MAP;

    static {
        // Initialize the genre map in a static block
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("28", "Action");
        tempMap.put("12", "Adventure");
        tempMap.put("16", "Animation");
        tempMap.put("35", "Comedy");
        tempMap.put("80", "Crime");
        tempMap.put("99", "Documentary");
        tempMap.put("18", "Drama");
        tempMap.put("10751", "Family");
        tempMap.put("14", "Fantasy");
        tempMap.put("36", "History");
        tempMap.put("27", "Horror");
        tempMap.put("10402", "Music");
        tempMap.put("9648", "Mystery");
        tempMap.put("10749", "Romance");
        tempMap.put("878", "Science Fiction");
        tempMap.put("10770", "TV Movie");
        tempMap.put("53", "Thriller");
        tempMap.put("10752", "War");
        tempMap.put("37", "Western");

        // Make the map unmodifiable to enforce immutability
        GENRE_MAP = Collections.unmodifiableMap(tempMap);
    }


}
