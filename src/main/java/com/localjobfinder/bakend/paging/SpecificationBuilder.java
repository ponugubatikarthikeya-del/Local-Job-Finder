package com.localjobfinder.bakend.paging;


import com.localjobfinder.bakend.apiDataPackage.JobFilterCriteria;
import com.localjobfinder.bakend.dbEntity.JobsEntity;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class SpecificationBuilder {

    public static Specification<JobsEntity> filterBy(
            JobFilterCriteria criteria, Double radiusInMeters, Point userLocation) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // 1. Spatial Boundary Constraint Evaluation via MySQL spatial functional index mapping
            if (userLocation != null && radiusInMeters != null) {
                Expression<Double> distanceExpression = cb.function(
                        "ST_Distance_Sphere",
                        Double.class,
                        root.get("location"),
                        cb.literal(userLocation)
                );

                predicates.add(cb.lessThanOrEqualTo(distanceExpression, radiusInMeters));
            }

            predicates.add(cb.isNotNull(root.get("location")));

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // 2. Freeform Full-Text Search Predicates
            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String searchPattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                Predicate title = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate description = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate category = cb.like(cb.lower(root.get("category")), searchPattern);
                predicates.add(cb.or(title, description, category));
            }

            // 3. Exact Category Node Alignment
            if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("category")),
                        criteria.getCategory().trim().toLowerCase()
                ));
            }

            // 4. Default Operational Status Filtering Lifecycle
            String targetStatus = "OPEN";
            predicates.add(cb.equal(root.get("status").as(String.class), targetStatus));

            // 5. Compensation Constraint Processing (Mapped to payAmount from schema)
            if (criteria.getSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("payAmount"), criteria.getSalary()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}