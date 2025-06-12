package edu.cda.project.ticklybackend.mappers;

import edu.cda.project.ticklybackend.dtos.event.EventCategoryDto;
import edu.cda.project.ticklybackend.models.event.EventCategory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventCategoryMapper {
    EventCategoryDto eventCategoryToEventCategoryDto(EventCategory eventCategory);

    List<EventCategoryDto> eventCategoriesToEventCategoryDtos(List<EventCategory> eventCategories);
}