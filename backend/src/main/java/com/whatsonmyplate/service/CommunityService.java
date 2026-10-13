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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    public static final int MAX_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_SIZE = 12;
    private static final String CLOUDINARY_FOLDER = "whatsonmyplate/community";
    private static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public CommunityService(
            CommunityPostRepository communityPostRepository,
            CommunityPostLikeRepository communityPostLikeRepository,
            CommentRepository commentRepository,
            RatingRepository ratingRepository,
            RecipeRepository recipeRepository,
            UserRepository userRepository,
            ImageStorageService imageStorageService
    ) {
        this.communityPostRepository = communityPostRepository;
        this.communityPostLikeRepository = communityPostLikeRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public CommunityPostPageResponse getCommunityPosts(int page, int size, String currentPrincipal) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero.");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE + ".");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityPost> postPage = communityPostRepository.findAllByOrderByCreatedAtDesc(pageable);

        User currentUser = resolveOptionalUser(currentPrincipal);

        List<CommunityPostResponse> content = postPage.getContent().stream()
                .map(post -> mapToResponse(post, currentUser))
                .collect(Collectors.toList());

        return new CommunityPostPageResponse(
                content,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public CommunityPostResponse getCommunityPostById(Long id, String currentPrincipal) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community post not found with id: " + id));

        User currentUser = resolveOptionalUser(currentPrincipal);
        return mapToResponse(post, currentUser);
    }

    public CommunityPostResponse createCommunityPost(
            String currentPrincipal,
            Long recipeId,
            String caption,
            Integer initialRating,
            MultipartFile image
    ) {
        User user = getAuthenticatedUser(currentPrincipal);
        validateImageFile(image);

        if (initialRating != null && (initialRating < 1 || initialRating > 5)) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5.");
        }

        Recipe recipe = null;
        if (recipeId != null) {
            recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + recipeId));
        }

        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read image content.", e);
        }

        // Upload to Cloudinary outside DB transaction
        ImageUploadResult uploadResult = imageStorageService.uploadImage(
                imageBytes,
                image.getContentType(),
                CLOUDINARY_FOLDER
        );

        CommunityPost post;
        try {
            post = saveCommunityPostInTransaction(user, recipe, uploadResult.getSecureUrl(), caption, initialRating);
        } catch (Exception e) {
            // Delete uploaded Cloudinary asset if DB persistence fails
            if (uploadResult.getPublicId() != null) {
                imageStorageService.deleteImage(uploadResult.getPublicId());
            }
            throw e;
        }

        return mapToResponse(post, user);
    }

    @Transactional
    public CommunityPost saveCommunityPostInTransaction(
            User user,
            Recipe recipe,
            String imageUrl,
            String caption,
            Integer initialRating
    ) {
        CommunityPost post = new CommunityPost(user, recipe, imageUrl, caption);
        CommunityPost savedPost = communityPostRepository.save(post);

        if (initialRating != null) {
            Rating rating = new Rating(user, savedPost, initialRating, null);
            ratingRepository.save(rating);
        }

        return savedPost;
    }

    @Transactional
    public CommunityPostResponse updateCommunityPost(
            String currentPrincipal,
            Long id,
            CommunityPostUpdateRequest request
    ) {
        User user = getAuthenticatedUser(currentPrincipal);
        CommunityPost post = communityPostRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Community post not found with id: " + id));

        post.setCaption(request.getCaption());
        CommunityPost updated = communityPostRepository.save(post);
        return mapToResponse(updated, user);
    }

    @Transactional
    public void deleteCommunityPost(String currentPrincipal, Long id) {
        User user = getAuthenticatedUser(currentPrincipal);
        CommunityPost post = communityPostRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Community post not found with id: " + id));

        communityPostRepository.delete(post);
    }

    @Transactional
    public LikeResponse likePost(String currentPrincipal, Long postId) {
        User user = getAuthenticatedUser(currentPrincipal);
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Community post not found with id: " + postId));

        if (!communityPostLikeRepository.existsByUserIdAndCommunityPostId(user.getId(), postId)) {
            CommunityPostLike like = new CommunityPostLike(user, post);
            communityPostLikeRepository.save(like);
        }

        long count = communityPostLikeRepository.countByCommunityPostId(postId);
        return new LikeResponse(true, count);
    }

    @Transactional
    public LikeResponse unlikePost(String currentPrincipal, Long postId) {
        User user = getAuthenticatedUser(currentPrincipal);
        if (!communityPostRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Community post not found with id: " + postId);
        }

        communityPostLikeRepository.deleteByUserIdAndCommunityPostId(user.getId(), postId);
        long count = communityPostLikeRepository.countByCommunityPostId(postId);
        return new LikeResponse(false, count);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        if (!communityPostRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Community post not found with id: " + postId);
        }

        return commentRepository.findByCommunityPostIdOrderByCreatedAtAsc(postId).stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse createComment(String currentPrincipal, Long postId, CommentCreateRequest request) {
        User user = getAuthenticatedUser(currentPrincipal);
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Community post not found with id: " + postId));

        Comment comment = new Comment(user, post, request.getContent().trim());
        Comment saved = commentRepository.save(comment);
        return mapToCommentResponse(saved);
    }

    @Transactional
    public void deleteComment(String currentPrincipal, Long commentId) {
        User user = getAuthenticatedUser(currentPrincipal);
        Comment comment = commentRepository.findByIdAndUserId(commentId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        commentRepository.delete(comment);
    }

    @Transactional
    public RatingResponse ratePost(String currentPrincipal, Long postId, RatingRequest request) {
        User user = getAuthenticatedUser(currentPrincipal);
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Community post not found with id: " + postId));

        Optional<Rating> existingRatingOpt = ratingRepository.findByUserIdAndCommunityPostId(user.getId(), postId);
        if (existingRatingOpt.isPresent()) {
            Rating existing = existingRatingOpt.get();
            existing.setScore(request.getScore());
            existing.setReview(request.getReview());
            ratingRepository.save(existing);
        } else {
            Rating newRating = new Rating(user, post, request.getScore(), request.getReview());
            ratingRepository.save(newRating);
        }

        Double avg = ratingRepository.findAverageScoreByCommunityPostId(postId);
        Long count = ratingRepository.countByCommunityPostId(postId);
        return new RatingResponse(avg, count, request.getScore());
    }

    @Transactional(readOnly = true)
    public RatingResponse getPostRating(Long postId, String currentPrincipal) {
        if (!communityPostRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Community post not found with id: " + postId);
        }

        Double avg = ratingRepository.findAverageScoreByCommunityPostId(postId);
        Long count = ratingRepository.countByCommunityPostId(postId);

        Integer userRating = null;
        User user = resolveOptionalUser(currentPrincipal);
        if (user != null) {
            userRating = ratingRepository.findByUserIdAndCommunityPostId(user.getId(), postId)
                    .map(Rating::getScore)
                    .orElse(null);
        }

        return new RatingResponse(avg, count, userRating);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Image size exceeds the maximum limit of 10MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported image type: '" + contentType + "'. Allowed types are: " + String.join(", ", ALLOWED_MIME_TYPES)
            );
        }
    }

    private User getAuthenticatedUser(String principalUsername) {
        return userRepository.findByEmail(principalUsername)
                .or(() -> userRepository.findByUsername(principalUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));
    }

    private User resolveOptionalUser(String principalUsername) {
        if (principalUsername == null || principalUsername.isBlank() || "anonymousUser".equalsIgnoreCase(principalUsername)) {
            return null;
        }
        return userRepository.findByEmail(principalUsername)
                .or(() -> userRepository.findByUsername(principalUsername))
                .orElse(null);
    }

    private CommunityPostResponse mapToResponse(CommunityPost post, User currentUser) {
        long likeCount = communityPostLikeRepository.countByCommunityPostId(post.getId());
        long commentCount = post.getComments() != null ? post.getComments().size() : 0L;
        Double averageRating = ratingRepository.findAverageScoreByCommunityPostId(post.getId());
        Long ratingCount = ratingRepository.countByCommunityPostId(post.getId());

        boolean isLiked = false;
        Integer currentUserRating = null;

        if (currentUser != null) {
            isLiked = communityPostLikeRepository.existsByUserIdAndCommunityPostId(currentUser.getId(), post.getId());
            currentUserRating = ratingRepository.findByUserIdAndCommunityPostId(currentUser.getId(), post.getId())
                    .map(Rating::getScore)
                    .orElse(null);
        }

        return new CommunityPostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getUsername(),
                post.getUser().getDisplayName(),
                post.getUser().getProfileImageUrl(),
                post.getRecipe() != null ? post.getRecipe().getId() : null,
                post.getRecipe() != null ? post.getRecipe().getTitle() : null,
                post.getImageUrl(),
                post.getCaption(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likeCount,
                commentCount,
                averageRating,
                ratingCount,
                isLiked,
                currentUserRating
        );
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getUser().getDisplayName(),
                comment.getUser().getProfileImageUrl(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
