package streakflix;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StreakFlixApplication {
    public static void main(String[] args) {

        SpringApplication.run(StreakFlixApplication.class, args);
    }
}