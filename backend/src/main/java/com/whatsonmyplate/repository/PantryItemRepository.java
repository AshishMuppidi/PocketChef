package com.whatsonmyplate.repository;

import com.whatsonmyplate.entity.PantryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {

    List<PantryItem> findByUserId(Long userId);

    List<PantryItem> findByUserIdOrderByNameAsc(Long userId);

    Optional<PantryItem> findByIdAndUserId(Long id, Long userId);

    Optional<PantryItem> findByUserIdAndNameIgnoreCase(Long userId, String name);

    void deleteByUserIdAndId(Long userId, Long id);
}
