package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.event.EventSearchParamsDto;
import edu.cda.project.ticklybackend.models.event.Event;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> getSpecification(EventSearchParamsDto params) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(params.getQuery())) {
                String likePattern = "%" + params.getQuery().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("shortDescription")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullDescription")), likePattern)
                ));
            }

            if (!CollectionUtils.isEmpty(params.getCategoryIds())) {
                predicates.add(root.join("categories").get("id").in(params.getCategoryIds()));
            }

            if (params.getStartDateAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), params.getStartDateAfter().toInstant()));
            }

            if (params.getStartDateBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), params.getStartDateBefore().toInstant()));
            }


            if (params.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), params.getStatus()));
            }

            if (params.getDisplayOnHomepage() != null) {
                predicates.add(criteriaBuilder.equal(root.get("displayOnHomepage"), params.getDisplayOnHomepage()));
            }

            if (params.getIsFeatured() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFeaturedEvent"), params.getIsFeatured()));
            }

            if (params.getStructureId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("structure").get("id"), params.getStructureId()));
            }

            if (StringUtils.hasText(params.getCity())) {
                predicates.add(criteriaBuilder.equal(root.get("address").get("city"), params.getCity()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

// TODO : Move to utils