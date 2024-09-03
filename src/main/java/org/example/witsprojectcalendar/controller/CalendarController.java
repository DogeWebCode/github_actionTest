package org.example.witsprojectcalendar.controller;

import com.google.api.services.calendar.model.Event;
import lombok.extern.log4j.Log4j2;
import org.example.witsprojectcalendar.data.dto.ErrorResponseDTO;
import org.example.witsprojectcalendar.data.dto.InsertEventRequest;
import org.example.witsprojectcalendar.data.dto.UpdateEventRequest;
import org.example.witsprojectcalendar.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
@Log4j2
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @PostMapping("/events")
    public ResponseEntity<?> insertEvents(@RequestBody InsertEventRequest request) throws GeneralSecurityException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String accessToken = authentication.getCredentials().toString();

        log.info("Received event creation request: {}", request);

        Event createdEvent = calendarService.insertEvent(accessToken, request);
        return ResponseEntity.ok(createdEvent);
    }

    @DeleteMapping("/events")
    public ResponseEntity<?> deleteEvents(@RequestParam String eventId) throws GeneralSecurityException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String accessToken = authentication.getCredentials().toString();

        boolean isDeleted = calendarService.deleteEvent(accessToken, eventId);
        return isDeleted ? ResponseEntity.noContent().build()
            : ResponseEntity.badRequest().body(new ErrorResponseDTO("Delete event failed"));
    }

    @GetMapping("/events")
    public ResponseEntity<?> getEvents(@RequestParam String startDate, @RequestParam String endDate) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String accessToken = authentication.getCredentials().toString();

        return ResponseEntity.ok(calendarService.getEvents(accessToken, startDate, endDate));
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable("eventId") String eventId, @RequestBody UpdateEventRequest request) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String accessToken = authentication.getCredentials().toString();

        Event updateEvent = calendarService.updateEvent(accessToken, eventId, request);

        return ResponseEntity.ok(updateEvent);
    }

    //used for heartbeat mechanism

    @GetMapping("/")
    public ResponseEntity<?> heartBeat() {
        return ResponseEntity.ok("healthy");
    }
}
