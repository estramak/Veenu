package controllers;

import dtos.CreateEventRequestDto;
import dtos.EventResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import services.EventService;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(
            @Valid @RequestBody CreateEventRequestDto request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        EventResponseDto result = eventService.createEvent(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
