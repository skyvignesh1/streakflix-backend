package streakflix.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import streakflix.model.FriendRequest;
import streakflix.model.Movie;
import streakflix.model.OTTDetails;
import streakflix.model.User;
import streakflix.repository.MovieRepository;
import streakflix.service.StreakFlixService;
import streakflix.util.BiasedRandom;
import streakflix.util.JwtUtil;


import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
public class StreakFlixController {

    @Autowired
    StreakFlixService service;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    MovieRepository movieRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User input) {
        log.info("User {} attempting to login", input.getUsername());
        Optional<User> userDetails;
        try {
            userDetails = service.login(input.getUsername(), input.getPassword());
            userDetails.ifPresent(user -> user.setAuthorizationToken(jwtUtil.generateToken(input.getUsername())));
        } catch (Exception e) {
            log.error("Error while logging in user {}", input.getUsername());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);

        }
        return new ResponseEntity<>(userDetails, HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User input) {
        log.info("User {} signing up", input.getUsername());
        try {
           service.signup(input);
        } catch (Exception e) {
            log.error("Error while signup {}", input.getUsername());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>("User created", HttpStatus.OK);
    }

    @PostMapping("/friendRequest")
    public ResponseEntity<?> friendRequest(@RequestBody FriendRequest friendRequest,@RequestHeader("Authorization") String authorizationHeader) {
        log.info("User {} got friend request", friendRequest.getExistingUsername());
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            if (jwtUtil.validateToken(token, username)) {
                service.sendFriendRequest(friendRequest);
            }else{
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error while sending friend request {}", friendRequest.getExistingUsername());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>("Friend Request has been sent", HttpStatus.OK);
    }

    @PostMapping("/friendAccept")
    public ResponseEntity<?> friendAccept(@RequestBody FriendRequest friendRequest,@RequestHeader("Authorization") String authorizationHeader) {
        log.info("User {} is accepting a friend request", friendRequest.getExistingUsername());
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            if (jwtUtil.validateToken(token, username)) {
                service.acceptFriendRequest(friendRequest);
            }else{
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            log.error("Error while accepting friend request {}", friendRequest.getExistingUsername());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>("Friend Request has been accepted", HttpStatus.OK);
    }

    @PostMapping("/updateTodayWatchedMinutes")
    public ResponseEntity<?> updateTodayWatchedMinutes(@RequestHeader("Authorization") String authorizationHeader,
                                                       @RequestBody Movie movie) {
        boolean alreadyCompletedMovie;
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            log.debug("Extracted token: {}", token);
            String username = jwtUtil.extractUsername(token);
            log.debug("Extracted username from token: {}", username);
            if (jwtUtil.validateToken(token, username)) {
                log.info("Token is valid for username: {}", username);
                alreadyCompletedMovie = service.updateTodayWatchedMinutes(username, movie);
                log.info("Updated today's watched minutes for user: {}", username);
            } else {
                log.warn("Invalid session for token: {}", token);
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Exception occurred while updating today's watched minutes", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(alreadyCompletedMovie, HttpStatus.OK);
    }

    @PostMapping("/validateToken")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("Received request to validate token");
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            log.debug("Extracted token: {}", token);
            String username = jwtUtil.extractUsername(token);
            log.debug("Extracted username from token: {}", username);
            if (jwtUtil.validateToken(token, username)) {
                log.info("Token is valid for username: {}", username);
                return new ResponseEntity<>(service.getUserDetailsByUsername(username), HttpStatus.OK);
            } else {
                log.warn("Invalid session for token: {}", token);
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Exception occurred while validating token", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/searchFriends")
    public ResponseEntity<?> searchFriends(@RequestParam("name") String name, @RequestHeader("Authorization") String authorizationHeader) {
        log.info("Received request to search friends with name: {}", name);
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            log.debug("Extracted token: {}", token);
            String username = jwtUtil.extractUsername(token);
            log.debug("Extracted username from token: {}", username);
            if (jwtUtil.validateToken(token, username)) {
                log.info("Token is valid for username: {}", username);
                return new ResponseEntity<>(service.findMatchingUsers(username, name), HttpStatus.OK);
            } else {
                log.warn("Invalid session for token: {}", token);
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Exception occurred while searching friends", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/listAllFriends")
    public ResponseEntity<?> listAllFriends(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("Received request to list all friends");
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            log.debug("Extracted token: {}", token);
            String username = jwtUtil.extractUsername(token);
            log.debug("Extracted username from token: {}", username);
            if (jwtUtil.validateToken(token, username)) {
                log.info("Token is valid for username: {}", username);
                return new ResponseEntity<>(service.listAllFriends(username), HttpStatus.OK);
            } else {
                log.warn("Invalid session for token: {}", token);
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Exception occurred while listing all friends", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/listAllFriendRequests")
    public ResponseEntity<?> listAllFriendRequests(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("Received request to list all friend requests");
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            log.debug("Extracted token: {}", token);
            String username = jwtUtil.extractUsername(token);
            log.debug("Extracted username from token: {}", username);
            if (jwtUtil.validateToken(token, username)) {
                log.info("Token is valid for username: {}", username);
                return new ResponseEntity<>(service.listAllFriendRequests(username), HttpStatus.OK);
            } else {
                log.warn("Invalid session for token: {}", token);
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Exception occurred while listing all friend requests", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/listAllPendingRequests")
    public ResponseEntity<?> listAllPendingRequests(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("Received request to list all pending requests");
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            log.debug("Extracted token: {}", token);
            String username = jwtUtil.extractUsername(token);
            log.debug("Extracted username from token: {}", username);
            if (jwtUtil.validateToken(token, username)) {
                log.info("Token is valid for username: {}", username);
                return new ResponseEntity<>(service.listAllPendingRequests(username), HttpStatus.OK);
            } else {
                log.warn("Invalid session for token: {}", token);
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Exception occurred while listing all pending requests", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Autowired
    BiasedRandom randomBias;

    @PostMapping("/addMovieMetaDataToMongoDb")
    public void receiveInfo(@RequestBody Movie movie){
        try{

            movie.setCompositeKey(new Movie.CompositeKey(String.valueOf(movie.getCompositeKey().getMovieId()), String.valueOf(randomBias.getOTTRandomPlatform(0))));
            movie.setStreakCount(randomBias.getRandom());
            movie.setMovieName(movie.getMovieName());
            movieRepository.save(movie);

            boolean shouldPresentInMultipleOttPlatforms = randomBias.biasedFalse();
            if(shouldPresentInMultipleOttPlatforms){
                movie.setCompositeKey(new Movie.CompositeKey(String.valueOf(movie.getCompositeKey().getMovieId()), String.valueOf(randomBias.getOTTRandomPlatform(Integer.parseInt(movie.getCompositeKey().getPlatform())))));
                movie.setStreakCount(randomBias.getRandom());
                movie.setMovieName(movie.getMovieName());
                movieRepository.save(movie);
            }
        }catch (Exception exp){
            System.out.println(exp.getMessage());
        }
    }

    @GetMapping("/getMovieDetails")
    public Movie getMovieDetails(@RequestParam("movieId") String movieId) throws Exception {
        try {
            return service.getMovieDetails(movieId);
        }catch (Exception e){
            return new Movie();
        }
    }
    @GetMapping("/pullAllMoviesFromMongoDb")
    public List<Movie> pullMoviesFromMongoDb(){
        return service.pullAllMoviesFromMongoDb();
    }

    @GetMapping("/pullAllOtt")
    public List<OTTDetails> getAllOTTs(){
        return service.pullAllOtt();
    }
    @GetMapping("/pullAllMoviesFromMongoDbByPlatform")
    public List<Movie> pullAllMoviesFromMongoDbByPlatform(@RequestParam(name = "platform") String platform){
        return service.pullAllMoviesFromMongoDbByPlatform(platform);
    }

    @GetMapping("/searchMoviesFromMongoDb")
    public List<Movie> searchMoviesFromMongoDb(@RequestParam(name = "keyword") String keyword){
        return service.searchMoviesFromMongoDb(keyword);
    }
}

