package edu.cda.project.ticklybackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.OrganizationServiceUser;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.ReservationServiceUser;
import edu.cda.project.ticklybackend.models.user.roles.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StructureAdministratorUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(
                SpectatorUser.class,
                ReservationServiceUser.class,
                OrganizationServiceUser.class,
                StructureAdministratorUser.class
                );
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}
