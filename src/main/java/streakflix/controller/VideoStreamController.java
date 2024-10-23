package streakflix.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import streakflix.service.VideoStreamService;

@RestController
@RequestMapping("/video")
@Component
public class VideoStreamController {

    private final VideoStreamService videoStreamService;

    public VideoStreamController(VideoStreamService videoStreamService) {
        this.videoStreamService = videoStreamService;
    }

    @GetMapping("/stream/{fileType}/{fileName}")
    public ResponseEntity<byte[]> streamVideo(@PathVariable String fileType,
                                              @PathVariable String fileName,
                                              @RequestHeader(value = "Range", required = false) String range,
                                              HttpServletResponse response) {
        return videoStreamService.prepareContent(fileName, fileType, range);
    }
}