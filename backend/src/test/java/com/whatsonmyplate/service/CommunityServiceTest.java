package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.community.CommentCreateRequest;
import com.whatsonmyplate.dto.community.CommentResponse;
import com.whatsonmyplate.dto.community.CommunityPostPageResponse;
import com.whatsonmyplate.dto.community.CommunityPostResponse;
import com.whatsonmyplate.dto.community.CommunityPostUpdateRequest;
import com.whatsonmyplate.dto.community.LikeResponse;
import com.whatsonmyplate.dto.community.RatingRequest;
import com.whatsonmyplate.dto.community.RatingResponse;
import com.whatsonmyplate.dto.storage.ImageUploadResult;
import com.whatsonmyplate.entity.Comment;
import com.whatsonmyplate.entity.CommunityPost;
import com.whatsonmyplate.entity.CommunityPostLike;
import com.whatsonmyplate.entity.Rating;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.User;
import com.whatsonmyplate.exception.ResourceNotFoundException;
import com.whatsonmyplate.repository.CommentRepository;
import com.whatsonmyplate.repository.CommunityPostLikeRepository;
import com.whatsonmyplate.repository.CommunityPostRepository;
import com.whatsonmyplate.repository.RatingRepository;
import com.whatsonmyplate.repository.RecipeRepository;
import com.whatsonmyplate.repository.UserRepository;
import com.whatsonmyplate.service.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private CommunityPostLikeRepository communityPostLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageService imageStorageService;

    private CommunityService communityService;

    private User testUser;

    @BeforeEach
    void setUp() {
        communityService = new CommunityService(
                communityPostRepository,
                communityPostLikeRepository,
                commentRepository,
                ratingRepository,
                recipeRepository,
                userRepository,
                imageStorageService
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("chefmaria");
        testUser.setEmail("maria@example.com");
        testUser.setDisplayName("Chef Maria");
    }

    @Test
    void testGetCommunityPosts() {
        CommunityPost post = new CommunityPost(testUser, null, "https://cloudinary.com/post.jpg", "Great remake!");
        post.setId(10L);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        Page<CommunityPost> page = new PageImpl<>(List.of(post));
        when(communityPostRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);
        when(communityPostLikeRepository.countByCommunityPostId(10L)).thenReturn(5L);
        when(ratingRepository.findAverageScoreByCommunityPostId(10L)).thenReturn(4.5);
        when(ratingRepository.countByCommunityPostId(10L)).thenReturn(2L);

        CommunityPostPageResponse res = communityService.getCommunityPosts(0, 12, null);

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        assertEquals(10L, res.getContent().get(0).getId());
        assertEquals("Great remake!", res.getContent().get(0).getCaption());
        assertEquals(5L, res.getContent().get(0).getLikeCount());
        assertEquals(4.5, res.getContent().get(0).getAverageRating());
    }

    @Test
    void testCreateCommunityPostSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "dish.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(testUser));
        when(imageStorageService.uploadImage(any(), eq("image/jpeg"), eq("whatsonmyplate/community")))
                .thenReturn(new ImageUploadResult("whatsonmyplate/community/dish123", "https://cloudinary.com/dish.jpg", "jpg", 1024L));

        CommunityPost post = new CommunityPost(testUser, null, "https://cloudinary.com/dish.jpg", "My first remake!");
        post.setId(20L);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        when(communityPostRepository.save(any(CommunityPost.class))).thenReturn(post);

        CommunityPostResponse response = communityService.createCommunityPost(
                "maria@example.com", null, "My first remake!", 5, file
        );

        assertNotNull(response);
        assertEquals(20L, response.getId());
        assertEquals("My first remake!", response.getCaption());
        assertEquals("https://cloudinary.com/dish.jpg", response.getImageUrl());
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void testLikePostToggle() {
        CommunityPost post = new CommunityPost(testUser, null, "https://cloudinary.com/dish.jpg", "Caption");
        post.setId(30L);

        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(testUser));
        when(communityPostRepository.findById(30L)).thenReturn(Optional.of(post));
        when(communityPostLikeRepository.existsByUserIdAndCommunityPostId(1L, 30L)).thenReturn(false);
        when(communityPostLikeRepository.countByCommunityPostId(30L)).thenReturn(1L);

        LikeResponse res = communityService.likePost("maria@example.com", 30L);

        assertTrue(res.getLiked());
        assertEquals(1L, res.getLikeCount());
        verify(communityPostLikeRepository).save(any(CommunityPostLike.class));
    }

    @Test
    void testCommentCreation() {
        CommunityPost post = new CommunityPost(testUser, null, "https://cloudinary.com/dish.jpg", "Caption");
        post.setId(40L);

        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(testUser));
        when(communityPostRepository.findById(40L)).thenReturn(Optional.of(post));

        Comment savedComment = new Comment(testUser, post, "Looks amazing!");
        savedComment.setId(50L);
        savedComment.setCreatedAt(LocalDateTime.now());
        savedComment.setUpdatedAt(LocalDateTime.now());

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponse res = communityService.createComment("maria@example.com", 40L, new CommentCreateRequest("Looks amazing!"));

        assertNotNull(res);
        assertEquals(50L, res.getId());
        assertEquals("Looks amazing!", res.getContent());
        assertEquals("chefmaria", res.getUsername());
    }

    @Test
    void testRatePost() {
        CommunityPost post = new CommunityPost(testUser, null, "https://cloudinary.com/dish.jpg", "Caption");
        post.setId(60L);

        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(testUser));
        when(communityPostRepository.findById(60L)).thenReturn(Optional.of(post));
        when(ratingRepository.findByUserIdAndCommunityPostId(1L, 60L)).thenReturn(Optional.empty());
        when(ratingRepository.findAverageScoreByCommunityPostId(60L)).thenReturn(5.0);
        when(ratingRepository.countByCommunityPostId(60L)).thenReturn(1L);

        RatingResponse res = communityService.ratePost("maria@example.com", 60L, new RatingRequest(5, "Perfect recipe"));

        assertNotNull(res);
        assertEquals(5.0, res.getAverageRating());
        assertEquals(1L, res.getRatingCount());
        assertEquals(5, res.getCurrentUserRating());
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void testDeletePostByOwnerSuccess() {
        CommunityPost post = new CommunityPost(testUser, null, "https://cloudinary.com/dish.jpg", "Caption");
        post.setId(70L);

        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(testUser));
        when(communityPostRepository.findByIdAndUserId(70L, 1L)).thenReturn(Optional.of(post));

        communityService.deleteCommunityPost("maria@example.com", 70L);

        verify(communityPostRepository).delete(post);
    }

    @Test
    void testDeletePostByNonOwnerThrows404() {
        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(testUser));
        when(communityPostRepository.findByIdAndUserId(70L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> communityService.deleteCommunityPost("maria@example.com", 70L));
    }
}
