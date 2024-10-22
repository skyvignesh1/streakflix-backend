package streakflix.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import streakflix.model.FriendRequest;
import streakflix.model.User;
import streakflix.service.StreakFlixService;


import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "http://localhost:4200")
public class StreakFlixController {

    @Autowired
    StreakFlixService service;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User input) {
        log.info("User {} attempting to login", input.getUsername());
        Optional<User> userDetails;
        try {
            userDetails = service.login(input.getUsername(), input.getPassword());
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
    public ResponseEntity<?> friendRequest(@RequestBody FriendRequest friendRequest) {
        log.info("User {} got friend request", friendRequest.getExistingUsername());
        try {
            service.sendFriendRequest(friendRequest);
        } catch (Exception e) {
            log.error("Error while sending friend request {}", friendRequest.getExistingUsername());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>("Friend Request has been sent", HttpStatus.OK);
    }

    @PostMapping("/friendAccept")
    public ResponseEntity<?> friendAccept(@RequestBody FriendRequest friendRequest) {
        log.info("User {} is accepting a friend request", friendRequest.getExistingUsername());
        try {
            service.acceptFriendRequest(friendRequest);
        } catch (Exception e) {
            log.error("Error while accepting friend request {}", friendRequest.getExistingUsername());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>("Friend Request has been accepted", HttpStatus.OK);
    }

}
