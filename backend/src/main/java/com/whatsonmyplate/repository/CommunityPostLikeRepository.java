package com.whatsonmyplate.repository;

import com.whatsonmyplate.entity.CommunityPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {

    long countByCommunityPostId(Long communityPostId);

    boolean existsByUserIdAndCommunityPostId(Long userId, Long communityPostId);

    Optional<CommunityPostLike> findByUserIdAndCommunityPostId(Long userId, Long communityPostId);

    void deleteByUserIdAndCommunityPostId(Long userId, Long communityPostId);
}
