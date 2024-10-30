package streakflix.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import streakflix.model.FriendRequest;
import streakflix.model.User;
import streakflix.service.StreakFlixService;
import streakflix.util.JwtUtil;


import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class StreakFlixController {

    @Autowired
    StreakFlixService service;

    @Autowired
    private JwtUtil jwtUtil;

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
    public ResponseEntity<?> updateTodayWatchedMinutes(@RequestBody User user, @RequestHeader("Authorization") String authorizationHeader){
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            if (jwtUtil.validateToken(token, username)) {
                service.updateTodayWatchedMinutes(user, username);
            }else{
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/validateToken")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader){
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            if (jwtUtil.validateToken(token, username)) {
                return new ResponseEntity<>(service.getUserDetailsByUsername(username), HttpStatus.OK);
            }else{
                log.error("auth failed");
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/searchFriends")
    public ResponseEntity<?> searchFriends(@RequestParam("name") String name, @RequestHeader("Authorization") String authorizationHeader){
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            if (jwtUtil.validateToken(token, username)) {
                return new ResponseEntity<>(service.getUserDetailsByUsername(name), HttpStatus.OK);
            }else{
                log.error("auth failed");
                return new ResponseEntity<>("Invalid session", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}
