-- =============================================================================
-- Flyway Migration: V2__community_likes.sql
-- Description: Add community post likes table for WhatsOnMyPlate
-- Target: MySQL 8.x
-- =============================================================================

CREATE TABLE IF NOT EXISTS community_post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    community_post_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_post FOREIGN KEY (community_post_id) REFERENCES community_posts(id) ON DELETE CASCADE,
    CONSTRAINT uk_post_likes_user_post UNIQUE (user_id, community_post_id),
    INDEX idx_post_likes_user (user_id),
    INDEX idx_post_likes_post (community_post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
