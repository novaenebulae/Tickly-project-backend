package edu.cda.project.ticklybackend.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class EventLocationKey implements Serializable {

    @Column(name = "event_id")
    @JsonIgnore
    Integer eventId;

    @Column(name = "location_id")
    @JsonIgnore
    Integer locationId;

}
