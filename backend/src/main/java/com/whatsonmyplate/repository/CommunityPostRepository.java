package com.whatsonmyplate.repository;

import com.whatsonmyplate.entity.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    Page<CommunityPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<CommunityPost> findAllByOrderByCreatedAtDesc();

    List<CommunityPost> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<CommunityPost> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);

    Optional<CommunityPost> findByIdAndUserId(Long id, Long userId);
}
