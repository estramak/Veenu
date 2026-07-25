package controllers;

import dtos.FeedItemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.FeedService;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public ResponseEntity<List<FeedItemDto>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type
    ) {
        return ResponseEntity.ok(feedService.getFeed(page, size, type));
    }
}
