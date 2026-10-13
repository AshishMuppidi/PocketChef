package com.whatsonmyplate.repository.specification;

import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class RecipeSpecification {

    private RecipeSpecification() {
    }

    /**
     * Constructs a composite dynamic JPA Specification for Recipe discovery.
     *
     * @param q Optional case-insensitive search keyword matching title or description.
     * @param source Optional RecipeSource enum filter.
     * @param spiceLevel Optional SpiceLevel enum filter.
     * @param maxPrepTime Optional upper bound on prepTimeMinutes.
     * @return Composable Specification instance.
     */
    public static Specification<Recipe> withCriteria(
            String q,
            RecipeSource source,
            SpiceLevel spiceLevel,
            Integer maxPrepTime
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.trim().isEmpty()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            if (source != null) {
                predicates.add(cb.equal(root.get("source"), source));
            }

            if (spiceLevel != null) {
                predicates.add(cb.equal(root.get("spiceLevel"), spiceLevel));
            }

            if (maxPrepTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("prepTimeMinutes"), maxPrepTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
