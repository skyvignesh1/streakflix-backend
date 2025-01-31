package streakflix.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import streakflix.model.*;
import streakflix.repository.MovieRepository;
import streakflix.repository.OttRepository;
import streakflix.repository.StreakRepository;
import streakflix.repository.UserRepository;
import streakflix.util.BiasedRandom;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class StreakFlixService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreakRepository streakRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private OttRepository ottRepository;


    public Optional<User> login(String username, String password) throws Exception {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            if (user.get().getPassword().equals(password)) {
                log.info("User {} is logged in", username);

                List<String> friendUsernames = Stream.of(user.get().getUsername()).collect(Collectors.toList());
                if (user.get().getFriendList() != null) {
                    friendUsernames.addAll(user.get().getFriendList().stream()
                            .map(FriendList::getUsername)
                            .collect(Collectors.toList()));
                }

                List<Streak> friendStreaks = streakRepository.findByUsernameIn(friendUsernames);
                if (user.get().getFriendList() != null) {
                    user.get().getFriendList().forEach(friend -> {
                        friendStreaks.stream()
                                .filter(streakObj -> streakObj.getUsername().equals(friend.getUsername()))
                                .findFirst()
                                .ifPresent(streakObj -> friend.setStreak(streakObj.getStreak()));
                    });
                }

                friendStreaks.stream()
                        .filter(streakObj -> streakObj.getUsername().equals(user.get().getUsername()))
                        .findFirst()
                        .ifPresent(streakObj -> user.get().setStreak(String.valueOf(streakObj.getStreak())));

                return user;
            } else {
                log.error("Password is incorrect");
                throw new Exception("Password is incorrect");
            }
        } else {
            log.error("User {} is not found", username);
            throw new Exception("User not found");
        }
    }

    public void signup(User user) throws Exception {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            log.error("User {} already exists", user.getUsername());
            throw new Exception("User already exists");
        } else {
            user.setFriendList(Collections.emptyList());
            userRepository.save(user);

            Streak streak = new Streak();
            streak.setUsername(user.getUsername());
            streak.setStreak("0");
            streakRepository.save(streak);
            log.info("User {} has been created", user.getUsername());
        }
    }

    public void sendFriendRequest(FriendRequest friendRequest) throws Exception {
        User friendUser = userRepository.findByUsername(friendRequest.getFriendUsername()).orElseThrow(() -> new Exception("Friend request cannot be sent as user not found"));
        log.info("User {} is found to send request", friendRequest.getFriendUsername());

        User existingUser = userRepository.findByUsername(friendRequest.getExistingUsername()).orElseThrow(() -> new Exception("Existing user not found"));
        FriendList friendlist = new FriendList();
        friendlist.setUsername(friendRequest.getFriendUsername());
        friendlist.setStatus("REQUEST_SENT");
        if (existingUser.getFriendList() == null) {
            existingUser.setFriendList(new ArrayList<>());
        }
        existingUser.getFriendList().add(friendlist);
        userRepository.save(existingUser);

        FriendList newFriend = new FriendList();
        newFriend.setUsername(friendRequest.getExistingUsername());
        newFriend.setStatus("REQUEST_RECEIVED");
        if (friendUser.getFriendList() == null) {
            friendUser.setFriendList(new ArrayList<>());
        }
        friendUser.getFriendList().add(newFriend);
        userRepository.save(friendUser);

    }

    public void acceptFriendRequest(FriendRequest friendRequest) throws Exception {
        User friendUser = userRepository.findByUsername(friendRequest.getFriendUsername()).orElseThrow(() -> new Exception("Friend request cannot be sent as user not found"));

        User currentUser = userRepository.findByUsername(friendRequest.getExistingUsername()).orElseThrow(() -> new Exception("Existing user not found"));
        FriendList friendListEntry = currentUser.getFriendList().stream()
                .filter(friend -> friend.getUsername().equals(friendRequest.getFriendUsername()))
                .findFirst()
                .orElseThrow(() -> new Exception("Friend request not found"));
        friendListEntry.setStatus("ACCEPTED");
        userRepository.save(currentUser);

        FriendList newFriendEntry = friendUser.getFriendList().stream()
                .filter(friend -> friend.getUsername().equals(friendRequest.getExistingUsername()))
                .findFirst()
                .orElseThrow(() -> new Exception("Friend request not found"));
        newFriendEntry.setStatus("ACCEPTED");
        userRepository.save(friendUser);

    }

    @Async
    public boolean updateTodayWatchedMinutes(String userName, Movie requestMovie) throws Exception {

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new Exception("User is not found"));

        final Movie fetchedMovie = movieRepository.findByCompositeKey(requestMovie.getCompositeKey());
        boolean alreadyWatchedMovie = user.getWatchedMovies().contains(fetchedMovie) || fetchedMovie.getStreakCount() == 0;
        if(alreadyWatchedMovie)
            return true;

        Optional<WatchDetails> optionalWatchDetails = user.getWatchDetails().stream()
                .filter(wDetails -> wDetails.getMovieId().equals(fetchedMovie.getCompositeKey().getMovieId()) &&
                        wDetails.getPlatformId().equals(fetchedMovie.getCompositeKey().getPlatform())).findFirst();

        WatchDetails watchDetails;
        if(optionalWatchDetails.isEmpty()) {
            WatchDetails newWatchDetails = new WatchDetails();
            newWatchDetails.setMovieId(fetchedMovie.getCompositeKey().getMovieId());
            newWatchDetails.setPlatformId(fetchedMovie.getCompositeKey().getPlatform());
            newWatchDetails.setMovieName(fetchedMovie.getMovieName());
            user.getWatchDetails().add(newWatchDetails);
            watchDetails = newWatchDetails;
        }else
            watchDetails = optionalWatchDetails.get();

        watchDetails.setTrackTime(watchDetails.getTrackTime() + 10);
        if (watchDetails.getTrackTime() >= 60) {
            watchDetails.setTrackTime(0);
            watchDetails.setTodayWatchedMinutes(watchDetails.getTodayWatchedMinutes() + 1);
            updateStreak(user, watchDetails, fetchedMovie);
        }
        userRepository.save(user);
        return false;
    }

    public User getUserDetailsByUsername(String userName) throws Exception {
        Optional<User> user = userRepository.findByUsername(userName);
        if (user.isPresent()) {
            List<String> friendUsernames = Stream.of(user.get().getUsername()).collect(Collectors.toList());
            if (user.get().getFriendList() != null) {
                friendUsernames.addAll(user.get().getFriendList().stream()
                        .map(FriendList::getUsername)
                        .toList());
            }

            List<Streak> friendStreaks = streakRepository.findByUsernameIn(friendUsernames);
            if (user.get().getFriendList() != null) {
                user.get().getFriendList().forEach(friend -> {
                    friendStreaks.stream()
                            .filter(streakObj -> streakObj.getUsername().equals(friend.getUsername()))
                            .findFirst()
                            .ifPresent(streakObj -> friend.setStreak(streakObj.getStreak()));
                });
            }

            friendStreaks.stream()
                    .filter(streakObj -> streakObj.getUsername().equals(user.get().getUsername()))
                    .findFirst()
                    .ifPresent(streakObj -> user.get().setStreak(String.valueOf(streakObj.getStreak())));

            return user.get();
        } else {
            throw new Exception("No user found");
        }
    }

    public List<User> findMatchingUsers(String currentUser, String searchKeyword) {
        User currUser = userRepository.findByUsername(currentUser)
                .orElseThrow(() -> new RuntimeException("No user found"));

        return userRepository.findMatchingUsers(searchKeyword).stream()
                .limit(10)
                .filter(user -> !user.getUsername().equalsIgnoreCase(currUser.getUsername()))
                .peek(searchResult -> {
                    streakRepository.findByUsername(searchResult.getUsername())
                            .ifPresent(streak -> searchResult.setStreak(streak.getStreak()));
                    searchResult.setStatus(getFriendStatus(currUser, searchResult));
                })
                .toList();
    }

    private String getFriendStatus(User currentUser, User searchResult) {
        for (FriendList currentUserFriend : currentUser.getFriendList()) {
            if (currentUserFriend.getUsername().equalsIgnoreCase(searchResult.getUsername())) {
                if (currentUserFriend.getStatus().equalsIgnoreCase("ACCEPTED")) {
                    return "FRIEND";
                } else if (currentUserFriend.getStatus().equalsIgnoreCase("REQUEST_SENT")) {
                    return "REQUESTED";
                }
            }
        }
        return "NOT_FRIEND";
    }

    public List<FriendList> listAllFriendRequests(String username) {
        return listFriends(username, "REQUEST_RECEIVED");
    }

    public List<FriendList> listAllPendingRequests(String username) {
        return listFriends(username, "REQUEST_SENT");
    }


    public List<FriendList> listAllFriends(String username) {
        return listFriends(username, "ACCEPTED");
    }

    private List<FriendList> listFriends(String username, String status) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("No user found"));
        List<FriendList> friends = user.getFriendList().stream()
                .filter(friendList -> friendList.getStatus().equalsIgnoreCase(status))
                .toList();

        if (friends.isEmpty())
            return new ArrayList<>();

        var friendListNames = friends.stream().map(FriendList::getUsername).toList();
        List<Streak> friendStreaks = streakRepository.findByUsernameIn(friendListNames);

        friends.forEach(friend -> {
            friendStreaks.stream()
                    .filter(streakObj -> streakObj.getUsername().equals(friend.getUsername()))
                    .findFirst()
                    .ifPresent(streakObj -> friend.setStreak(streakObj.getStreak()));
        });


        return friends;
    }

    @Scheduled(cron = "0 59 23 * * ?")
    public void updateStreaks() {
        log.info("Cron Job started : Updating streaks");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.getWatchDetails().clear();
            userRepository.save(user);
        }
    }

    private void updateStreak(User user, WatchDetails watchDetails, Movie movie) {

        int movieDuration = movie.getActualDuration();
        int watchedDuration = watchDetails.getTodayWatchedMinutes();

        int watchedPercentage = (watchedDuration * 100 / movieDuration) ;
        if (watchedPercentage >= 5) {
            int movieStreak = movie.getStreakCount();
            Optional<Streak> streak = streakRepository.findByUsername(user.getUsername());
            if (streak.isPresent()) {
                Streak streakObj = streak.get();
                streakObj.setStreak(String.valueOf(Integer.parseInt(streakObj.getStreak()) + movieStreak));
                streakRepository.save(streakObj);
            }
            user.getWatchedMovies().add(movie);
        }
    }

    public Movie getMovieDetails(String movieId) throws Exception {
        return movieRepository.findByCompositeKeyMovieId(movieId).get(0);
    }
    public List<Movie> pullAllMoviesFromMongoDb() {
        return movieRepository.findAll();
    }

    public List<OTTDetails> pullAllOtt() {
        return ottRepository.findAll();
    }

    public List<Movie> pullAllMoviesFromMongoDbByPlatform(String platform) {
        return movieRepository.findByCompositeKeyPlatform(platform);
    }
    public List<Movie> searchMoviesFromMongoDb(String keyword) {
        return movieRepository.findByMovieNameOrderByStreakCountDesc(keyword);
    }
//
//    public void updateGenre(String movieId, String genre) {
//        List<Movie> movies = movieRepository.findByCompositeKeyMovieId(movieId);
//
//        for(Movie movie : movies){
//            movie.setGenre(genre);
//            movieRepository.save(movie);
//        }
//    }

    @Autowired
    BiasedRandom biasedRandom;

    public String getBannerSrcImg(String userName){

        User user = userRepository.findByUsername(userName).orElseThrow();

        if(user.getUserGenres() == null){
            return defaultBanner();
        }
        List<Movie> movies = new ArrayList<>();
        for(String keys : user.getUserGenres()){
            movies.addAll(movieRepository.findByGenre(keys));
        }
        if(movies.isEmpty()){
            return defaultBanner();
        }

        int totalSize = movies.size();
        int r = biasedRandom.getRandomNumber(totalSize);
        return movies.get(r-1).getMoviePosterURL();
    }


    // Default Banner from Action genre movies
    private String defaultBanner(){

        var movies = movieRepository.findByGenre("Action");
        int r = biasedRandom.getRandomNumber(movies.size());

        return movies.get(r-1).getMoviePosterURL();
    }

    public void addGenreToUser(AddGenre addGenre, String username){

        User user2 = userRepository.findByUsername(username).orElseThrow();
        if(user2.getUserGenres() == null)
            user2.setUserGenres(new ArrayList<>());
        else
            user2.getUserGenres().clear();

        List<String> genres = addGenre.getGenre();
        List<String> resultGenres;
        if (genres.contains("Animation")) {
            resultGenres = genres.stream()
                    .filter(genre -> genre.equals("Animation"))
                    .collect(Collectors.toList());
        } else {
            resultGenres = new ArrayList<>(genres);
        }

        user2.setUserGenres(resultGenres.stream().filter(r -> !r.equalsIgnoreCase("Documentary")).toList());
        userRepository.save(user2);
    }


}
