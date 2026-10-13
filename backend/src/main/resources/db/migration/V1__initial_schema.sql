-- =============================================================================
-- Flyway Migration: V1__initial_schema.sql
-- Description: Initial schema for WhatsOnMyPlate
-- Target: MySQL 8.x
-- =============================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    profile_image_url VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Recipes Table (with MySQL native JSON for ingredients and instructions)
CREATE TABLE IF NOT EXISTS recipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    source VARCHAR(50) NOT NULL DEFAULT 'USER_CREATED',
    source_id VARCHAR(100),
    is_ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
    prep_time_minutes INT,
    cook_time_minutes INT,
    servings INT,
    spice_level VARCHAR(20) DEFAULT 'MEDIUM',
    ingredients JSON,
    instructions JSON,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Pantry Items Table
CREATE TABLE IF NOT EXISTS pantry_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    quantity DOUBLE,
    unit VARCHAR(50),
    expiration_date DATE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_pantry_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_pantry_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Community Posts Table (Remakes of Recipes)
CREATE TABLE IF NOT EXISTS community_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT,
    image_url VARCHAR(500) NOT NULL,
    caption TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_community_posts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_posts_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE SET NULL,
    INDEX idx_community_user (user_id),
    INDEX idx_community_recipe (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Comments Table
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    community_post_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_post FOREIGN KEY (community_post_id) REFERENCES community_posts(id) ON DELETE CASCADE,
    INDEX idx_comments_post (community_post_id),
    INDEX idx_comments_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Ratings Table (Exact single-target: Recipe OR CommunityPost)
CREATE TABLE IF NOT EXISTS ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT,
    community_post_id BIGINT,
    score INT NOT NULL,
    review TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_ratings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_post FOREIGN KEY (community_post_id) REFERENCES community_posts(id) ON DELETE CASCADE,
    CONSTRAINT chk_rating_score CHECK (score >= 1 AND score <= 5),
    CONSTRAINT chk_rating_target CHECK ((recipe_id IS NOT NULL AND community_post_id IS NULL) OR (recipe_id IS NULL AND community_post_id IS NOT NULL)),
    CONSTRAINT uk_ratings_user_recipe UNIQUE (user_id, recipe_id),
    CONSTRAINT uk_ratings_user_post UNIQUE (user_id, community_post_id),
    INDEX idx_ratings_user (user_id),
    INDEX idx_ratings_recipe (recipe_id),
    INDEX idx_ratings_post (community_post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Saved Recipes Table (Bookmarks)
CREATE TABLE IF NOT EXISTS saved_recipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_saved_recipes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_recipes_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT uk_saved_recipes_user_recipe UNIQUE (user_id, recipe_id),
    INDEX idx_saved_recipes_user (user_id),
    INDEX idx_saved_recipes_recipe (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
