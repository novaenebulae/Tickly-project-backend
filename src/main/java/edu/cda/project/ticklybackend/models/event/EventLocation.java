package edu.cda.project.ticklybackend.models.event;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import edu.cda.project.ticklybackend.models.structure.Location;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "event_location")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class EventLocation {

    @EmbeddedId
    EventLocationKey id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    @JsonBackReference("event-locations")
    @JsonIgnore
    Event event;

    @ManyToOne
    @MapsId("locationId")
    @JoinColumn(name = "location_id")
    Location location;


}
