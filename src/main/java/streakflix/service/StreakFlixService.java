package streakflix.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import streakflix.model.FriendList;
import streakflix.model.FriendRequest;
import streakflix.model.Streak;
import streakflix.model.User;
import streakflix.repository.StreakRepository;
import streakflix.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class StreakFlixService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreakRepository streakRepository;

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

    public void updateTodayWatchedMinutes(User user, String userName) throws Exception {

        var user1 = userRepository.findByUsername(userName)
                .orElseThrow(() -> new Exception("User is not found"));

        user1.setTodayWatchedMinutes(user.getTodayWatchedMinutes());
        userRepository.save(user1);
    }

    public User getUserDetailsByUsername(String userName) throws Exception {
        Optional<User> user = userRepository.findByUsername(userName);
        if (user.isPresent()) {
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

                return user.get();
        }else{
            throw new Exception("No user found");
        }
    }

    public List<User> findMatchingUsers(String currentUser, String username){

        User currUser = userRepository.findByUsername(currentUser).orElseThrow(()->new RuntimeException("No user found"));
        var res = userRepository.findMatchingUsers(username).stream()
                .limit(10)
                .filter(user -> !user.getUsername().equalsIgnoreCase(currUser.getUsername()))
                .map(user -> {
                        boolean found = false;
                        if(currUser.getFriendList() != null){

                            for(FriendList u : currUser.getFriendList()){
                                if(user.getUsername().equalsIgnoreCase(u.getUsername())) {
                                    found = true;
                                    if (u.getStatus().equalsIgnoreCase("ACCEPTED"))
                                        user.setStatus("FRIEND");
                                    else if (u.getStatus().equalsIgnoreCase("REQUEST_SENT"))
                                        user.setStatus("REQUESTED");
                                    break;
                                }
                            }
                        }
                        if (!found) user.setStatus("NOT_FRIEND");
                        return user;
        });

        return res.toList();
    }

    public List<FriendList> listAllFriendRequests(String username){
        return listFriends(username, "REQUEST_RECEIVED");
    }

    public List<FriendList> listAllFriends(String username){
        return listFriends(username, "ACCEPTED");
    }
    private List<FriendList> listFriends(String username, String status){

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("No user found") );
        List<FriendList> friends = user.getFriendList().stream()
                .filter(friendList -> friendList.getStatus().equalsIgnoreCase(status))
                .toList();

        if(friends.isEmpty())
            return new ArrayList<>();

        var friendListNames = friends.stream().map(FriendList::getUsername).toList();
        List<Streak> friendStreaks = streakRepository.findByUsernameIn(friendListNames);

        friends.forEach(friend -> {
            friendStreaks.stream()
                    .filter(streakObj -> streakObj.getUsername().equals(friend.getUsername()))
                    .findFirst()
                    .ifPresent(streakObj -> friend.setStreak(streakObj.getStreak()));
        });


        return user.getFriendList();
    }

}
