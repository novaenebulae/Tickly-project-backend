package edu.cda.project.ticklybackend.services;

import edu.cda.project.ticklybackend.daos.eventDao.EventDao;
import edu.cda.project.ticklybackend.models.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventDao eventDao;

    @Autowired
    public EventService(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    public List<Event> findAllEvents() {
        return eventDao.findAll();
    }
}
