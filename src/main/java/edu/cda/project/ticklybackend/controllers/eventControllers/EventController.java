package edu.cda.project.ticklybackend.controllers.eventControllers;

import edu.cda.project.ticklybackend.daos.eventDao.EventDao;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventDao eventDao;

    @Autowired
    public EventController(EventService eventService, EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventDao.findAll();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Integer id) {
        Event event = eventDao.findEventById(id);

        if (event == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

}
