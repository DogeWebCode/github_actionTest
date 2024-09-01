package org.example.witsprojectcalendar.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.log4j.Log4j2;
import org.example.witsprojectcalendar.data.dto.InsertEventRequest;
import org.example.witsprojectcalendar.data.dto.UpdateEventRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class CalendarService {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final Cache<String, Calendar> calendarCache;

    public CalendarService() {
        this.calendarCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(55, TimeUnit.MINUTES)
            .build();

    }

    private Calendar getCalendarService(String accessToken) throws IOException {
        try {
            return calendarCache.get(accessToken, () -> createCalendarService(accessToken));
        } catch (ExecutionException e) {
            throw new IOException("Failed to create Calendar service", e);
        }
    }

    private Calendar createCalendarService(String accessToken) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
            .setAccessToken(accessToken);

        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME).build();
    }

    public boolean quickAddEvent(String accessToken, String title) throws IOException {
        Calendar service = getCalendarService(accessToken);
        Event quickAdd = service.events().quickAdd("primary", title).execute();
        log.info("quickAdd :{} ", quickAdd.toString());
        return true;
    }

    public boolean deleteEvent(String accessToken, String id) throws IOException {
        Calendar service = getCalendarService(accessToken);

        service.events().delete("primary", id).execute();
        return true;
    }

    public Event insertEvent(String accessToken, InsertEventRequest request) throws IOException {
        Calendar service = getCalendarService(accessToken);
        Event event = new Event()
            .setSummary(request.getNewSummary())
            .setDescription(request.getNewDescription());

        if (request.isAllDay()) {
            LocalDate startDate = parseDate(request.getNewStart());
            LocalDate endDate = parseDate(request.getNewEnd());

            event.setStart(new EventDateTime().setDate(new DateTime(startDate.toString())))
                .setEnd(new EventDateTime().setDate(new DateTime(endDate.toString())));
        } else {
            Instant startInstant = parseDateTime(request.getNewStart());
            Instant endInstant = parseDateTime(request.getNewEnd());

            event.setStart(new EventDateTime().setDateTime(new DateTime(startInstant.toEpochMilli())))
                .setEnd(new EventDateTime().setDateTime(new DateTime(endInstant.toEpochMilli())));
        }

        Event createdEvent = service.events().insert("primary", event).execute();
        return createdEvent;
    }


    public List<Event> getEvents(String accessToken, String startDate, String endDate) throws IOException {
        Calendar service = getCalendarService(accessToken);

        DateTime minTime = convertUtcToTimezone(startDate, "Asia/Taipei");
        DateTime maxTime = convertUtcToTimezone(endDate, "Asia/Taipei");

        Events events = service.events().list("primary")
            .setTimeMin(minTime)
            .setTimeMax(maxTime)
            .setOrderBy("startTime").setSingleEvents(true).execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
            }
        }
        return items;
    }

    public Event updateEvent(String accessToken, String eventId, UpdateEventRequest request) throws IOException {
        Calendar service = getCalendarService(accessToken);


        Event event = new Event()
            .setSummary(request.getNewSummary())
            .setDescription(request.getNewDescription());

        if (request.isAllDay()) {
            LocalDate startDate = parseDate(request.getNewStart());
            LocalDate endDate = parseDate(request.getNewEnd());
            event.setStart(new EventDateTime().setDate(new DateTime(startDate.toString())))
                .setEnd(new EventDateTime().setDate(new DateTime(endDate.toString())));
        } else {
            Instant startInstant = parseDateTime(request.getNewStart());
            Instant endInstant = parseDateTime(request.getNewEnd());

            event.setStart(new EventDateTime().setDateTime(new DateTime(startInstant.toEpochMilli())))
                .setEnd(new EventDateTime().setDateTime(new DateTime(endInstant.toEpochMilli())));
        }

        Event updatedEvent = service.events().update("primary", eventId, event).execute();
        return updatedEvent;
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            return Instant.parse(dateString).atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }

    private Instant parseDateTime(String dateTimeString) {
        return Instant.parse(dateTimeString);
    }

    private DateTime convertUtcToTimezone(String utcDateTimeString, String targetTimezone) {
        Instant instant = Instant.parse(utcDateTimeString);

        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(targetTimezone));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        String dateTimeString = zonedDateTime.format(formatter);

        return new DateTime(dateTimeString);
    }
}

