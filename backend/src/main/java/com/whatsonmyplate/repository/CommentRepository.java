package com.whatsonmyplate.repository;

import com.whatsonmyplate.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByCommunityPostIdOrderByCreatedAtAsc(Long communityPostId);

    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Comment> findByIdAndUserId(Long id, Long userId);
}
