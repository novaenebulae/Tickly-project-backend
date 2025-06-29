package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.structure.StructureSearchParamsDto;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class StructureSpecification {

    public static Specification<Structure> getSpecification(StructureSearchParamsDto params) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtre par nom (query)
            if (StringUtils.hasText(params.getQuery())) {
                String likePattern = "%" + params.getQuery().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern));
            }

            // Filtre par type
            if (!CollectionUtils.isEmpty(params.getTypeIds())) {
                Join<Structure, StructureType> typesJoin = root.join("types");

                predicates.add(typesJoin.get("id").in(params.getTypeIds()));
            }

            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}