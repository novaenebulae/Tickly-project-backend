package edu.cda.project.ticklybackend.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.cda.project.ticklybackend.models.structure.Location;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "event_location")
public class EventLocation {

    @EmbeddedId
    @JsonIgnore
    EventLocationKey id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    @JsonIgnore
    Event event;

    @ManyToOne
    @MapsId("locationId")
    @JoinColumn(name = "location_id")
    Location location;


}
